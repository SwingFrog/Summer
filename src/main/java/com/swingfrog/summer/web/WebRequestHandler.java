package com.swingfrog.summer.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.util.ForwardedAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.concurrent.MatchGroupKey;
import com.swingfrog.summer.concurrent.SessionQueueMgr;
import com.swingfrog.summer.concurrent.SingleQueueMgr;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.server.RemoteDispatchMgr;
import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.CodeMsg;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.server.rpc.RpcClientMgr;
import com.swingfrog.summer.web.view.FileView;
import com.swingfrog.summer.web.view.WebView;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class WebRequestHandler extends SimpleChannelInboundHandler<HttpObject> {

	private static final Logger log = LoggerFactory.getLogger(WebRequestHandler.class);
	private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	private final ServerContext serverContext;
	private HttpRequest httpRequest;
	private HttpPostRequestDecoder postRequestDecoder;
	
	public WebRequestHandler(ServerContext serverContext) {
		this.serverContext = serverContext;
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		if (serverContext.getConfig().isAllowAddressEnable()) {
			String address = ((InetSocketAddress)ctx.channel().remoteAddress()).getHostString();
			String[] addressList = serverContext.getConfig().getAllowAddressList();
			boolean allow = false;
			for (String s : addressList) {
				if (address.equals(s)) {
					allow = true;
					break;
				}
			}
			if (!allow) {
				log.warn("not allow {} connect", address);
				ctx.close();
				return;
			}
			log.info("allow {} connect", address);
		}
		serverContext.getSessionContextGroup().createSession(ctx);
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		sctx.clearSessionId();
		if (!serverContext.getSessionHandlerGroup().accpet(sctx)) {
			log.warn("not accept client {}", sctx);
			ctx.close();
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		log.info("added client {}", sctx);
		serverContext.getSessionHandlerGroup().added(sctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		if (sctx != null) {
			log.info("removed client {}", sctx);
			serverContext.getSessionHandlerGroup().removed(sctx);
			serverContext.getSessionContextGroup().destroySession(ctx);
			RpcClientMgr.get().remove(sctx);
			SessionQueueMgr.get().shutdown(sctx);
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) {
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		long now = Calendar.getInstance().getTimeInMillis();
		long last = sctx.getLastRecvTime();
		sctx.setLastRecvTime(now);
		if ((now - last) < serverContext.getConfig().getColdDownMs()) {
			serverContext.getSessionHandlerGroup().sendTooFastMsg(sctx);
		}
		sctx.setHeartCount(0);
		try {
			if (httpObject instanceof HttpRequest) {
				httpRequest = (HttpRequest) httpObject;
				if (!httpRequest.decoderResult().isSuccess()) {
					return;
				}
				String uri = httpRequest.uri();
				if (uri == null) {
					return;
				}
				sctx.setSessionId(parseSessionId(httpRequest.headers().get(HttpHeaderNames.COOKIE)));
				sctx.setRealAddress(ForwardedAddressUtil.parse(httpRequest.headers().get(ForwardedAddressUtil.KEY)));
				HttpMethod method = httpRequest.method();
				if (HttpMethod.GET.equals(method)) {
					doGet(ctx, sctx);
				} else if (HttpMethod.POST.equals(method)) {
					postRequestDecoder = new HttpPostRequestDecoder(factory, httpRequest);
					processHttpContent(ctx, sctx, (HttpContent) httpObject);
				} else {
					log.warn("not found request method[{}]", method.name());
				}
			} else if (httpObject instanceof HttpContent) {
				processHttpContent(ctx, sctx, (HttpContent) httpObject);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			serverContext.getSessionHandlerGroup().unableParseMsg(sctx);
		}
	}
	
	private void processHttpContent(ChannelHandlerContext ctx, SessionContext sctx, HttpContent httpContent) {
		if (postRequestDecoder != null) {
			try {
				postRequestDecoder.offer(httpContent);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return;
			}
			if (httpContent instanceof LastHttpContent) {
				doPost(ctx, sctx);
				postRequestDecoder.destroy();
				postRequestDecoder = null;
				httpRequest = null;
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		if (cause instanceof TooLongFrameException) {
			serverContext.getSessionHandlerGroup().lengthTooLongMsg(sctx);
		} else {
			log.error(cause.getMessage(), cause);
		}
	}
	
	private WebRequest getWebRequest() {
		String uri = httpRequest.uri();
		if (uri.length() == 1) {
			uri = uri + WebMgr.get().getIndex();
		}
		if (WebMgr.DEFAULT_FAVICON.equals(uri)) {
			uri = "/" + WebMgr.get().getFavicon();
		}
		try {
			uri = URLDecoder.decode(uri, serverContext.getConfig().getCharset());
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return WebRequest.build(httpRequest, uri);
	}
	
	private void doGet(ChannelHandlerContext ctx, SessionContext sctx) {
		WebRequest webRequest = getWebRequest();
		if (webRequest.isDynamic()) {
			doWork(ctx, sctx, webRequest);
		} else {
			doFile(ctx, sctx, webRequest);
		}
	}
	
	private void doPost(ChannelHandlerContext ctx, SessionContext sctx) {
		WebRequest webRequest = getWebRequest();
		JSONObject data = webRequest.getData();
		try {
			while (postRequestDecoder.hasNext()) {
				InterfaceHttpData httpData = postRequestDecoder.next();
				try {
					if (httpData.getHttpDataType() == HttpDataType.Attribute || 
							httpData.getHttpDataType() == HttpDataType.InternalAttribute) {
						Attribute attribute = (Attribute) httpData;
						data.put(attribute.getName(), attribute.getValue());
					} else if (httpData.getHttpDataType() == HttpDataType.FileUpload) {
						FileUpload fileUpload = (FileUpload) httpData;
						if (fileUpload.isCompleted()) {
							webRequest.getFileUploadMap().put(fileUpload.getName(), new WebFileUpload(fileUpload));
						} else {
							log.error("fileUpload not complete name[{}]", fileUpload.getName());
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				} finally {
					postRequestDecoder.removeHttpDataFromClean(httpData);
					httpData.release();
				}
			}
		} catch (EndOfDataDecoderException ignored) {
			
		}
		if (webRequest.isDynamic()) {
			doWork(ctx, sctx, webRequest);
		} else {
			doFile(ctx, sctx, webRequest);
		}
	}
	
	private void doFile(ChannelHandlerContext ctx, SessionContext sctx, WebRequest request) {
		log.debug("server request {} from {}", request.getPath(), sctx);
		ExecutorService eventExecutor = serverContext.getEventExecutor();
		eventExecutor.execute(()->{
			try {
				writeResponse(ctx, sctx, request, new FileView(WebMgr.get().getWebContentPath() + request.getPath()));
			} catch (IOException e) {
				writeResponse(ctx, sctx, request, WebMgr.get().getInteriorViewFactory().createErrorView(404, "Not Found"));
			}
		});
	}
	
	private void doWork(ChannelHandlerContext ctx, SessionContext sctx, WebRequest request) {
		log.debug("server request {} from {}", request, sctx);
		if (serverContext.getSessionHandlerGroup().receive(sctx, request)) {
			RemoteStatistics.start(request, 0);
			Runnable event = ()-> {
				try {
					ProcessResult<WebView> processResult = RemoteDispatchMgr.get().webProcess(serverContext, request, sctx);
					if (processResult.isAsync()) {
						return;
					}
					WebView webView = processResult.getValue();
					if (webView == null) {
						writeResponse(ctx, sctx, request, WebMgr.get().getInteriorViewFactory().createBlankView());
					} else {
						webView.ready();
						writeResponse(ctx, sctx, request, webView);
					}
				} catch (CodeException ce) {
					log.warn(ce.getMessage(), ce);
					WebView webView = WebMgr.get().getInteriorViewFactory().createErrorView(500, ce.getCode(), ce.getMsg());
					writeResponse(ctx, sctx, request, webView);
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
					CodeMsg ce = SessionException.INVOKE_ERROR;
					WebView webView = WebMgr.get().getInteriorViewFactory().createErrorView(500, ce.getCode(), ce.getMsg());
					writeResponse(ctx, sctx, request, webView);
				}
				RemoteStatistics.finish(request, 0);
			};
			Method method = RemoteDispatchMgr.get().getMethod(request);
			if (method != null) {
				MatchGroupKey matchGroupKey = ContainerMgr.get().getSingleQueueKey(method);
				if (matchGroupKey != null) {
					if (matchGroupKey.hasKeys()) {
						Object[] partKeys = new Object[matchGroupKey.getKeys().size()];
						for (int i = 0; i < matchGroupKey.getKeys().size(); i++) {
							String key = request.getData().getString(matchGroupKey.getKeys().get(i));
							if (key == null) {
								key = "";
							}
							partKeys[i] = key;
						}
						SingleQueueMgr.get().execute(matchGroupKey.getMainKey(partKeys).intern(), event);
					} else {									
						SingleQueueMgr.get().execute(matchGroupKey.getMainKey().intern(), event);
					}
				} else {
					if (ContainerMgr.get().isSessionQueue(method)) {
						SessionQueueMgr.get().execute(sctx, event);
					} else {
						serverContext.getEventExecutor().execute(event);
					}
				}
			} else {
				serverContext.getEventExecutor().execute(event);
			}
		}
	}

	private void writeResponse(ChannelHandlerContext ctx, SessionContext sctx, WebRequest request, WebView webView) {
		log.debug("server response {} status[{}] from {}", webView, webView.getStatus(), sctx);
		write(ctx, sctx, request, webView);
	}

	public static void write(ChannelHandlerContext ctx, SessionContext sctx, WebRequest request, WebView webView) {
		try {
			DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, 
					HttpResponseStatus.valueOf(webView.getStatus()));
			if (HttpUtil.isKeepAlive(request.getHttpRequest())) {
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, webView.getContentType());
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH, webView.getLength());
			response.headers().set(HttpHeaderNames.SERVER, Summer.NAME);
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			if (sctx.getSessionId() == null) {
				response.headers().set(HttpHeaderNames.SET_COOKIE, createSessionId());
			}
			if (webView.getHeaders() != null) {
				webView.getHeaders().forEach((key, value) -> response.headers().set(key, value));
			}
			ctx.write(response);
			ctx.write(webView.getChunkedInput());
			ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static String createSessionId() {
		return "sessionId=" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}

	private static String parseSessionId(String cookie) {
		if (cookie == null)
			return null;
		String sessionId = "sessionId=";
		int index = cookie.indexOf(sessionId);
		if (index == -1)
			return null;
		if (index + sessionId.length() + 32 > cookie.length()) {
			return null;
		}
		return cookie.substring(index + sessionId.length(), index + sessionId.length() + 32);
	}

}

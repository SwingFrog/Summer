package com.swingfrog.summer.server;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Calendar;

import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.statistics.RemoteStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.concurrent.MatchGroupKey;
import com.swingfrog.summer.concurrent.SessionQueueMgr;
import com.swingfrog.summer.concurrent.SingleQueueMgr;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.server.rpc.RpcClientMgr;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;

public class ServerStringHandler extends SimpleChannelInboundHandler<String> {
	
	private static final Logger log = LoggerFactory.getLogger(ServerStringHandler.class);
	private final ServerContext serverContext;
	
	public ServerStringHandler(ServerContext serverContext) {
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
		if (serverContext.getSessionHandlerGroup().accept(sctx)) {
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
	protected void channelRead0(ChannelHandlerContext ctx, String msg) {
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		long now = Calendar.getInstance().getTimeInMillis();
		long last = sctx.getLastRecvTime();
		sctx.setLastRecvTime(now);
		if ((now - last) < serverContext.getConfig().getColdDownMs()) {
			serverContext.getSessionHandlerGroup().sendTooFastMsg(sctx);
		}
		sctx.setHeartCount(0);
		if ("ping".equals(msg)) {
			ctx.writeAndFlush("pong");
		} else if (msg.startsWith("rpc")) {
			String[] msgs = msg.split("\t");
			RpcClientMgr.get().add(sctx, msgs[1], msgs[2]);
		} else {
			try {
				SessionRequest request = JSON.parseObject(msg, SessionRequest.class);
				if (request.getId() != sctx.getCurrentMsgId()) {
					sctx.setCurrentMsgId(request.getId());
					log.debug("server request {} from {}", msg, sctx);
					if (serverContext.getSessionHandlerGroup().receive(sctx, request)) {
						RemoteStatistics.start(request, msg.length());
						Runnable event = ()->{
							if (!ctx.channel().isActive()) {
								RemoteStatistics.discard(request);
								return;
							}
							try {
								ProcessResult<SessionResponse> processResult = RemoteDispatchMgr.get().process(serverContext, request, sctx);
								if (processResult.isAsync()) {
									return;
								}
								String response = processResult.getValue().toJSONString();
								log.debug("server response {} to {}", response, sctx);
								writeResponse(ctx, sctx, response);
								RemoteStatistics.finish(request, response.length());
							} catch (CodeException ce) {
								log.warn(ce.getMessage(), ce);
								String response = SessionResponse.buildError(request, ce).toJSONString();
								log.debug("server response error {} to {}", response, sctx);
								writeResponse(ctx, sctx, response);
								RemoteStatistics.finish(request, response.length());
							} catch (Throwable e) {
								log.error(e.getMessage(), e);
								String response = SessionResponse.buildError(request, SessionException.INVOKE_ERROR).toJSONString();
								log.debug("server response error {} to {}", response, sctx);
								writeResponse(ctx, sctx, response);
								RemoteStatistics.finish(request, response.length());
							}
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
				} else {
					serverContext.getSessionHandlerGroup().repetitionMsg(sctx);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				serverContext.getSessionHandlerGroup().unableParseMsg(sctx);
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

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		super.channelWritabilityChanged(ctx);
		SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
		while (ctx.channel().isActive() && ctx.channel().isWritable() && sctx.getWaitWriteQueueSize() > 0) {
			ctx.writeAndFlush(sctx.getWaitWriteQueue().poll());
		}
	}


	private void writeResponse(ChannelHandlerContext ctx, SessionContext sctx, String response) {
		write(ctx, serverContext, sctx, response);
	}

	public static void write(ChannelHandlerContext ctx, ServerContext serverContext, SessionContext sctx, String response) {
		if (!ctx.channel().isActive()) {
			return;
		}
		if (sctx.getWaitWriteQueueSize() == 0 && ctx.channel().isWritable()) {
			ctx.writeAndFlush(response);
		} else {
			sctx.getWaitWriteQueue().add(response);
		}
		serverContext.getSessionHandlerGroup().sending(sctx);
	}

}

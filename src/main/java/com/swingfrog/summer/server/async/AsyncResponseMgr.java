package com.swingfrog.summer.server.async;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.protobuf.ErrorCodeProtobufBuilder;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.server.Server;
import com.swingfrog.summer.server.ServerMgr;
import com.swingfrog.summer.server.ServerWriteHelper;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.CodeMsg;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.web.WebMgr;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.WebRequestHandler;
import com.swingfrog.summer.web.view.TextView;
import com.swingfrog.summer.web.view.WebView;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class AsyncResponseMgr {

    private static final Logger log = LoggerFactory.getLogger(AsyncResponseMgr.class);

    private static class SingleCase {
        public static final AsyncResponseMgr INSTANCE = new AsyncResponseMgr();
    }

    private AsyncResponseMgr() {

    }

    public static AsyncResponseMgr get() {
        return AsyncResponseMgr.SingleCase.INSTANCE;
    }

    public void process(SessionContext sctx, SessionRequest request, Supplier<Object> runnable) {
        try {
            sendResponse(sctx, request, runnable.get());
        } catch (CodeException ce) {
            log.warn(ce.getMessage(), ce);
            sendErrorResponse(sctx, request, ce.getCode(), ce.getMsg());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            CodeMsg codeMsg = SessionException.INVOKE_ERROR;
            sendErrorResponse(sctx, request, codeMsg.getCode(), codeMsg.getMsg());
        }
    }

    public void process(SessionContext sctx, SessionRequest request, Runnable runnable) {
        try {
            runnable.run();
            sendResponse(sctx, request, null);
        } catch (CodeException ce) {
            log.warn(ce.getMessage(), ce);
            sendErrorResponse(sctx, request, ce.getCode(), ce.getMsg());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            CodeMsg codeMsg = SessionException.INVOKE_ERROR;
            sendErrorResponse(sctx, request, codeMsg.getCode(), codeMsg.getMsg());
        }
    }

    public void process(SessionContext sctx, ProtobufRequest request, Supplier<? extends Message> runnable) {
        try {
            sendResponse(sctx, request, runnable.get());
        } catch (CodeException ce) {
            log.warn(ce.getMessage(), ce);
            sendErrorResponse(sctx, request, ce.getCode(), ce.getMsg());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            CodeMsg codeMsg = SessionException.INVOKE_ERROR;
            sendErrorResponse(sctx, request, codeMsg.getCode(), codeMsg.getMsg());
        }
    }

    private void sendResponse(SessionContext sctx, SessionRequest request, Object data) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        ChannelHandlerContext ctx = server.getServerContext().getSessionContextGroup().getChannelBySession(sctx);
        String protocol = server.getServerContext().getConfig().getProtocol();
        if (ProtocolConst.isHttp(protocol)) {
            WebView webView;
            if (data == null) {
                webView = WebMgr.get().getInteriorViewFactory().createBlankView();
                log.debug("server async response {} status[{}] from {}", webView, webView.getStatus(), sctx);
                WebRequestHandler.write(ctx, sctx, (WebRequest) request, webView);
                return;
            }
            if (data instanceof WebView) {
                webView = (WebView) data;
            } else {
                webView = new TextView(JSON.toJSONString(data));
            }
            log.debug("server async response {} status[{}] from {}", webView, webView.getStatus(), sctx);
            try {
                webView.ready();
                WebRequestHandler.write(ctx, sctx, (WebRequest) request, webView);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            RemoteStatistics.finish(sctx, request, 0);
        } else {
            String response = SessionResponse.buildMsg(request, data).toJSONString();
            log.debug("server async response {} to {}", response, sctx);
            ServerWriteHelper.write(ctx, server.getServerContext(), sctx, response);
            RemoteStatistics.finish(sctx, request, response.length());
        }
    }

    private void sendErrorResponse(SessionContext sctx, SessionRequest request, long code, String msg) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        String protocol = server.getServerContext().getConfig().getProtocol();
        if (ProtocolConst.isHttp(protocol)) {
            log.error("Http protocol can't send error response");
            return;
        }
        ChannelHandlerContext ctx = server.getServerContext().getSessionContextGroup().getChannelBySession(sctx);
        String response = SessionResponse.buildError(request, code, msg).toJSONString();
        log.debug("server async response error {} to {}", response, sctx);
        ServerWriteHelper.write(ctx, server.getServerContext(), sctx, response);
        RemoteStatistics.finish(sctx, request, response.length());
    }

    private void sendResponse(SessionContext sctx, ProtobufRequest request, Message response) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        ChannelHandlerContext ctx = server.getServerContext().getSessionContextGroup().getChannelBySession(sctx);
        log.debug("server async response {} to {}", response, sctx);
        ServerWriteHelper.write(ctx, server.getServerContext(), sctx, Protobuf.of(request.getId(), response));
        RemoteStatistics.finish(sctx, request, response.getSerializedSize());
    }

    private void sendErrorResponse(SessionContext sctx, ProtobufRequest request, long code, String msg) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        ChannelHandlerContext ctx = server.getServerContext().getSessionContextGroup().getChannelBySession(sctx);
        Message response = ErrorCodeProtobufBuilder.build(request.getId(), code, msg);
        log.debug("server async response error {} to {}", response, sctx);
        ServerWriteHelper.write(ctx, server.getServerContext(), sctx, response);
        RemoteStatistics.finish(sctx, request, response.getSerializedSize());
    }

}

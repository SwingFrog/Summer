package com.swingfrog.summer.server.async;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.protocol.protobuf.ErrorCodeProtobufBuilder;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.protocol.tiny.msg.TinyError;
import com.swingfrog.summer.protocol.tiny.msg.TinyResp;
import com.swingfrog.summer.server.*;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.CodeMsg;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.web.WebMgr;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.WebRequestHandler;
import com.swingfrog.summer.web.view.TextView;
import com.swingfrog.summer.web.view.WebView;
import io.netty.channel.Channel;
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

    public void sendResponse(SessionContext sctx, SessionRequest request, Object data) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        ServerContext serverContext = server.getServerContext();

        Channel channel = server.getChannel(sctx);
        if (serverContext.isTiny()) {
            TinyResp response;
            if (data instanceof TinyResp) {
                response = (TinyResp) data;
            } else {
                response = TinyResp.ofJSON(data);
            }
            log.debug("server async response {} to {}", response, sctx);
            ServerWriteHelper.write(serverContext, sctx, response);
            RemoteStatistics.finish(sctx, request, response.getLength());
        } else if (serverContext.isHttp()) {
            WebView webView;
            if (data == null) {
                webView = WebMgr.get().getInteriorViewFactory().createBlankView();
                log.debug("server async response {} status[{}] from {}", webView, webView.getStatus(), sctx);
                WebRequestHandler.write(serverContext, channel, sctx, (WebRequest) request, webView);
                return;
            }
            if (data instanceof WebView) {
                webView = (WebView) data;
            } else {
                webView = new TextView(JSON.toJSONString(data));
            }
            log.debug("server async response {} status[{}] from {}", webView, webView.getStatus(), sctx);
            try {
                WebRequestHandler.write(serverContext, channel, sctx, (WebRequest) request, webView);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            RemoteStatistics.finish(sctx, request, 0);
        } else {
            if (serverContext.isProtobuf()) {
                return;
            }
            String response = SessionResponse.buildMsg(request, data).toJSONString();
            log.debug("server async response {} to {}", response, sctx);
            ServerWriteHelper.write(serverContext, sctx, response);
            RemoteStatistics.finish(sctx, request, response.length());
        }
    }

    public void sendErrorResponse(SessionContext sctx, SessionRequest request, int code, String msg) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        ServerContext serverContext = server.getServerContext();
        if (serverContext.isTiny()) {
            TinyError response = TinyError.of(code, msg);
            log.debug("server async response error {} to {}", response, sctx);
            ServerWriteHelper.write(serverContext, sctx, response);
            RemoteStatistics.finish(sctx, request, response.getLength());
            return;
        } else if (serverContext.isHttp()) {
            log.error("Http protocol can't send error response");
            return;
        }
        String response = SessionResponse.buildError(request, code, msg).toJSONString();
        log.debug("server async response error {} to {}", response, sctx);
        ServerWriteHelper.write(serverContext, sctx, response);
        RemoteStatistics.finish(sctx, request, response.length());
    }

    public void sendResponse(SessionContext sctx, ProtobufRequest request, Message response) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        log.debug("server async response {} to {}", response, sctx);
        ServerWriteHelper.write(server.getServerContext(), sctx, Protobuf.of(request.getId(), response));
        RemoteStatistics.finish(sctx, request, response.getSerializedSize());
    }

    public void sendErrorResponse(SessionContext sctx, ProtobufRequest request, int code, String msg) {
        Server server = ServerMgr.get().findServer(sctx);
        if (server == null) {
            log.error("Async send response failure. cause: can't found server by session context");
            return;
        }
        Message response = ErrorCodeProtobufBuilder.build(request.getId(), code, msg);
        log.debug("server async response error {} to {}", response, sctx);
        ServerWriteHelper.write(server.getServerContext(), sctx, Protobuf.of(ProtocolConst.PROTOBUF_ERROR_CODE_RESP_ID, response));
        RemoteStatistics.finish(sctx, request, response.getSerializedSize());
    }

}

package com.swingfrog.summer.server;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.tiny.msg.Tiny;
import com.swingfrog.summer.protocol.tiny.msg.TinyError;
import com.swingfrog.summer.protocol.tiny.msg.TinyReq;
import com.swingfrog.summer.protocol.tiny.msg.TinyResp;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.struct.AutowireParam;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTinyHandler extends AbstractServerHandler<TinyReq> {

    private static final Logger log = LoggerFactory.getLogger(ServerTinyHandler.class);

    protected ServerTinyHandler(ServerContext serverContext) {
        super(serverContext);
    }

    @Override
    protected void recv(Channel channel, SessionContext sctx, TinyReq req) {
        String charset = serverContext.getConfig().getCharset();
        try {
            int id = req.getId();
            String msg = req.getMsg();
            RemoteTinyDispatchMgr.RemoteMethod remoteMethod = RemoteTinyDispatchMgr.get().getRemote(id);
            SessionRequest request = new SessionRequest();
            request.setRemote(remoteMethod.getRemote());
            request.setMethod(remoteMethod.getMethod());
            request.setData(JSON.parseObject(msg));

            log.debug("server request {} from {}", msg, sctx);
            if (!sessionHandlerGroup.receive(sctx, request)) {
                return;
            }

            AutowireParam autowireParam = new AutowireParam();
            sessionHandlerGroup.autowireParam(sctx, autowireParam);

            RemoteStatistics.start(sctx, request, msg.length());
            Runnable runnable = () -> {
                if (!channel.isActive()) {
                    RemoteStatistics.discard(sctx, request);
                    return;
                }
                try {
                    sessionHandlerGroup.handleReady(sctx, request);

                    ProcessResult<TinyResp> processResult = RemoteTinyDispatchMgr.get().process(serverContext, request, sctx, autowireParam);
                    if (processResult.isAsync()) {
                        return;
                    }
                    TinyResp response = processResult.getValue();
                    log.debug("server response {} to {}", response, sctx);
                    writeResponse(sctx, response);
                    RemoteStatistics.finish(sctx, request, response.getLength(charset));
                } catch (CodeException ce) {
                    log.warn(ce.getMessage(), ce);
                    TinyError response = TinyError.of(ce);
                    log.debug("server response error {} to {}", response, sctx);
                    writeResponse(sctx, response);
                    RemoteStatistics.finish(sctx, request, response.getLength(charset));
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    TinyError response = TinyError.of(SessionException.INVOKE_ERROR);
                    log.debug("server response error {} to {}", response, sctx);
                    writeResponse(sctx, response);
                    RemoteStatistics.finish(sctx, request, response.getLength(charset));
                }
            };
            submitRunnable(sctx, request, runnable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sessionHandlerGroup.unableParseMsg(sctx);
        }
    }

    private void writeResponse(SessionContext sctx, Tiny response) {
        ServerWriteHelper.write(serverContext, sctx, response);
    }

}

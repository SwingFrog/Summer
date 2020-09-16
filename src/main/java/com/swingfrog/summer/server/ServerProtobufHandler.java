package com.swingfrog.summer.server;

import com.google.protobuf.Message;
import com.swingfrog.summer.concurrent.SessionQueueMgr;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.protocol.protobuf.ProtobufResponse;
import com.swingfrog.summer.protocol.protobuf.proto.CommonProto;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.struct.AutowireParam;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerProtobufHandler extends AbstractServerHandler<Protobuf> {

    private static final Logger log = LoggerFactory.getLogger(ServerProtobufHandler.class);

    protected ServerProtobufHandler(ServerContext serverContext) {
        super(serverContext);
    }

    @Override
    protected void recv(ChannelHandlerContext ctx, SessionContext sctx, Protobuf protobuf) throws Exception {
        int messageId = protobuf.getId();
        if (messageId == ProtocolConst.PROTOBUF_HEART_BEAT_REQ_ID) {
            CommonProto.HearBeat_Resp_0 resp = CommonProto.HearBeat_Resp_0
                    .newBuilder()
                    .setTime(System.currentTimeMillis())
                    .build();
            ctx.writeAndFlush(new Protobuf(messageId, resp.toByteArray()));
            return;
        }
        try {
            ProtobufRequest request = ProtobufRequest.of(messageId);
            log.debug("server request messageId[{}] from {}", messageId, sctx);
            if (!serverContext.getSessionHandlerGroup().receive(sctx, request)) {
                return;
            }

            AutowireParam autowireParam = new AutowireParam();
            serverContext.getSessionHandlerGroup().autowireParam(sctx, autowireParam);

            //RemoteStatistics.start(sctx, request, msg.length());
            Runnable runnable = () -> {
                if (!ctx.channel().isActive()) {
                    //RemoteStatistics.discard(sctx, request);
                    return;
                }
                try {
                    ProcessResult<ProtobufResponse> processResult = RemoteProtobufDispatchMgr.get().process(serverContext, request, protobuf, sctx, autowireParam);
                    if (processResult.isAsync()) {
                        return;
                    }
                    ProtobufResponse response = processResult.getValue();
                    log.debug("server response {} to {}", response, sctx);
                    writeResponse(ctx, sctx, new Protobuf(response.getId(), response.getMessage().toByteArray()));
                    //RemoteStatistics.finish(sctx, request, response.length());
                } catch (CodeException ce) {
//                    log.warn(ce.getMessage(), ce);
//                    String response = SessionResponse.buildError(request, ce).toJSONString();
//                    log.debug("server response error {} to {}", response, sctx);
//                    writeResponse(ctx, sctx, response);
                    //RemoteStatistics.finish(sctx, request, response.length());
                } catch (Throwable e) {
//                    log.error(e.getMessage(), e);
//                    String response = SessionResponse.buildError(request, SessionException.INVOKE_ERROR).toJSONString();
//                    log.debug("server response error {} to {}", response, sctx);
//                    writeResponse(ctx, sctx, response);
                    //RemoteStatistics.finish(sctx, request, response.length());
                }
            };

            SessionQueueMgr.get().execute(sctx, runnable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            serverContext.getSessionHandlerGroup().unableParseMsg(sctx);
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

    private void writeResponse(ChannelHandlerContext ctx, SessionContext sctx, Protobuf response) {
        ServerWriteHelper.write(ctx, serverContext, sctx, response);
    }

}

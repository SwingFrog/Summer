package com.swingfrog.summer.server;

import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.protobuf.ErrorCodeProtobufBuilder;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.protocol.protobuf.proto.CommonProto;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.statistics.RemoteStatistics;
import com.swingfrog.summer.struct.AutowireParam;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerProtobufHandler extends AbstractServerHandler<Protobuf> {

    private static final Logger log = LoggerFactory.getLogger(ServerProtobufHandler.class);

    protected ServerProtobufHandler(ServerContext serverContext) {
        super(serverContext);
    }

    @Override
    protected void recv(Channel channel, SessionContext sctx, Protobuf protobuf) {
        int messageId = protobuf.getId();
        if (messageId == ProtocolConst.PROTOBUF_HEART_BEAT_REQ_ID) {
            CommonProto.HeartBeat_Resp_0 resp = CommonProto.HeartBeat_Resp_0
                    .newBuilder()
                    .setTime(System.currentTimeMillis())
                    .build();
            channel.writeAndFlush(Protobuf.of(messageId, resp.toByteArray()));
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

            RemoteStatistics.start(sctx, request, protobuf.getBytes().length);
            Runnable runnable = () -> {
                if (!channel.isActive()) {
                    RemoteStatistics.discard(sctx, request);
                    return;
                }
                try {
                    Message reqMessage = RemoteProtobufDispatchMgr.get().parse(protobuf);
                    log.debug("server request {} from {}", reqMessage, sctx);
                    ProcessResult<Message> processResult = RemoteProtobufDispatchMgr.get().process(serverContext, request, reqMessage, sctx, autowireParam);
                    if (processResult == null) {
                        return;
                    }
                    if (processResult.isAsync()) {
                        return;
                    }
                    Message response = processResult.getValue();
                    log.debug("server response {} to {}", response, sctx);
                    writeResponse(sctx, Protobuf.of(messageId, response));
                    RemoteStatistics.finish(sctx, request, response.getSerializedSize());
                } catch (CodeException ce) {
                    log.warn(ce.getMessage(), ce);
                    Message response = ErrorCodeProtobufBuilder.build(messageId, ce);
                    log.debug("server response error {} to {}", response, sctx);
                    writeResponse(sctx, Protobuf.of(ProtocolConst.PROTOBUF_ERROR_CODE_RESP_ID, response));
                    RemoteStatistics.finish(sctx, request, response.getSerializedSize());
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    Message response = ErrorCodeProtobufBuilder.build(messageId, SessionException.INVOKE_ERROR);
                    log.debug("server response error {} to {}", response, sctx);
                    writeResponse(sctx, Protobuf.of(ProtocolConst.PROTOBUF_ERROR_CODE_RESP_ID, response));
                    RemoteStatistics.finish(sctx, request, response.getSerializedSize());
                }
            };
            submitSessionQueue(sctx, runnable);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            serverContext.getSessionHandlerGroup().unableParseMsg(sctx);
        }
    }

    private void writeResponse(SessionContext sctx, Protobuf protobuf) {
        ServerWriteHelper.write(serverContext, sctx, protobuf);
    }

}

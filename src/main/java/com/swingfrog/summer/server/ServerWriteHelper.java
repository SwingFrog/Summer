package com.swingfrog.summer.server;

import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.tiny.msg.Tiny;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerWriteHelper {

    private static final Logger log = LoggerFactory.getLogger(ServerWriteHelper.class);

    public static void write(ServerContext serverContext, SessionContext sctx, Object data) {
        if (data instanceof Tiny) {
            if (!serverContext.isTiny()) {
                log.error("can't write tiny data in the non-tiny protocol");
                return;
            }
        } else if (data instanceof Protobuf) {
            if (!serverContext.isProtobuf()) {
                log.error("can't write protobuf data in the non-protobuf protocol");
                return;
            }
        }
        Channel channel = sctx.getChannel();
        if (!channel.isActive()) {
            return;
        }
        if (sctx.getWaitWriteQueue().isEmpty() && channel.isWritable()) {
            channel.writeAndFlush(data);
        } else {
            sctx.getWaitWriteQueue().add(data);
        }
        serverContext.getSessionHandlerGroup().sending(sctx);
    }

}

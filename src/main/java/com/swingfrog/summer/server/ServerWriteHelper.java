package com.swingfrog.summer.server;

import com.swingfrog.summer.protocol.protobuf.Protobuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerWriteHelper {

    private static final Logger log = LoggerFactory.getLogger(ServerWriteHelper.class);

    public static void write(ChannelHandlerContext ctx, ServerContext serverContext, SessionContext sctx, Object data) {
        if (serverContext.isProtobuf()) {
            if (data instanceof Protobuf) {

            } else {
                log.error("can't write other data under the Protobuf protocol");
                return;
            }
        }
        if (!ctx.channel().isActive()) {
            return;
        }
        if (sctx.getWaitWriteQueueSize() == 0 && ctx.channel().isWritable()) {
            ctx.writeAndFlush(data);
        } else {
            sctx.getWaitWriteQueue().add(data);
        }
        serverContext.getSessionHandlerGroup().sending(sctx);
    }

}

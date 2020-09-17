package com.swingfrog.summer.server;

import io.netty.channel.ChannelHandlerContext;

public class ServerWriteHelper {

    public static void write(ChannelHandlerContext ctx, ServerContext serverContext, SessionContext sctx, Object data) {
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

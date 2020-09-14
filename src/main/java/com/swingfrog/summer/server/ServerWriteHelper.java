package com.swingfrog.summer.server;

import io.netty.channel.ChannelHandlerContext;

public class ServerWriteHelper {

    public static void write(ChannelHandlerContext ctx, ServerContext serverContext, SessionContext sctx, Object response) {
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

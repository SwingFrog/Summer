package com.swingfrog.summer.server;

import com.swingfrog.summer.protocol.protobuf.Protobuf;
import io.netty.channel.ChannelHandlerContext;

public class ServerProtobufHandler extends AbstractServerHandler<Protobuf> {

    protected ServerProtobufHandler(ServerContext serverContext) {
        super(serverContext);
    }

    @Override
    protected void recv(ChannelHandlerContext ctx, SessionContext sctx, Protobuf request) throws Exception {

    }

}

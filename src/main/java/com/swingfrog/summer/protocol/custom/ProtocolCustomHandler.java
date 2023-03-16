package com.swingfrog.summer.protocol.custom;

import com.swingfrog.summer.server.ServerContext;
import io.netty.channel.socket.SocketChannel;

public interface ProtocolCustomHandler {

    void onHandle(SocketChannel socketChannel, ServerContext serverContext);

}

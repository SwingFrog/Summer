package com.swingfrog.summer.protocol.custom;

import com.swingfrog.summer.server.ServerContext;
import io.netty.channel.socket.SocketChannel;

public class ProtocolCustomMgr {

    private ProtocolCustomHandler handler;

    private static class SingleCase {
        public static final ProtocolCustomMgr INSTANCE = new ProtocolCustomMgr();
    }

    private ProtocolCustomMgr() {

    }

    public static ProtocolCustomMgr get() {
        return ProtocolCustomMgr.SingleCase.INSTANCE;
    }

    public void setHandler(ProtocolCustomHandler handler) {
        this.handler = handler;
    }

    public boolean hasHandler() {
        return handler != null;
    }

    public void onHandle(SocketChannel socketChannel, ServerContext serverContext) {
        handler.onHandle(socketChannel, serverContext);
    }

}

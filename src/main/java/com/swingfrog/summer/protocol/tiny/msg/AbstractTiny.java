package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;

public abstract class AbstractTiny implements Tiny {

    private ByteBuf byteBuf;
    private int length = -1;

    @Override
    public ByteBuf getByteBuf(String charset) throws Exception {
        if (byteBuf == null) {
            byteBuf = toByteBuf(charset);
            length = byteBuf.readableBytes();
        }
        return byteBuf;
    }

    @Override
    public int getLength(String charset) {
        if (byteBuf == null) {
            try {
                getByteBuf(charset);
            } catch (Exception ignored) {}
        }
        return length;
    }

    protected abstract ByteBuf toByteBuf(String charset) throws Exception;

}

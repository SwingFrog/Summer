package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public abstract class AbstractTiny implements Tiny {

    private ByteBuf byteBuf;
    private int length = -1;

    @Override
    public ByteBuf getByteBuf(ByteBufAllocator alloc, String charset) throws Exception {
        if (byteBuf == null) {
            byteBuf = toByteBuf(alloc, charset);
            length = byteBuf.readableBytes();
        }
        return byteBuf;
    }

    @Override
    public int getLength() {
        return length;
    }

    protected abstract ByteBuf toByteBuf(ByteBufAllocator alloc, String charset) throws Exception;

}

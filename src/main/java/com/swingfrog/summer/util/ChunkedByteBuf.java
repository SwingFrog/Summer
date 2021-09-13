package com.swingfrog.summer.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;

public class ChunkedByteBuf implements ChunkedInput<ByteBuf> {

    private final ByteBuf byteBuf;

    public ChunkedByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public boolean isEndOfInput() {
        return true;
    }

    @Override
    public void close() {
        byteBuf.clear();
    }

    @Override
    public ByteBuf readChunk(ChannelHandlerContext ctx) {
        return null;
    }

    @Override
    public ByteBuf readChunk(ByteBufAllocator allocator) {
        return byteBuf;
    }

    @Override
    public long length() {
        return byteBuf.readableBytes();
    }

    @Override
    public long progress() {
        return 0;
    }

}

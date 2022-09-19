package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public interface Tiny {

    ByteBuf getByteBuf(ByteBufAllocator alloc, String charset) throws Exception;
    int getLength();

}

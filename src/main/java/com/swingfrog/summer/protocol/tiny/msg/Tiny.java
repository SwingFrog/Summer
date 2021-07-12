package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;

public interface Tiny {

    ByteBuf getByteBuf(String charset) throws Exception;
    int getLength(String charset);

}

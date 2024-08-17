package com.swingfrog.summer.util;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;

public class ByteBufOutputStream extends OutputStream {

    private final ByteBuf byteBuf;

    public ByteBufOutputStream(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public void write(int b) throws IOException {
        byteBuf.writeByte(b);
    }

}

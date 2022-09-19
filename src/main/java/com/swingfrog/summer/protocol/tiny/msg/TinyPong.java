package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class TinyPong extends AbstractTiny  {

    private final long time;

    public static TinyPong of() {
        return new TinyPong(System.currentTimeMillis());
    }

    public TinyPong(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "TinyPong{" +
                "time=" + time +
                '}';
    }

    @Override
    public ByteBuf toByteBuf(ByteBufAllocator alloc, String charset) {
        ByteBuf buf = alloc.directBuffer(8);
        buf.writeLong(time);
        return buf;
    }

}

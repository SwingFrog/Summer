package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TinyPong extends AbstractTiny  {

    private final long time;

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
    public ByteBuf toByteBuf(String charset) {
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeLong(time);
        return buf;
    }

}

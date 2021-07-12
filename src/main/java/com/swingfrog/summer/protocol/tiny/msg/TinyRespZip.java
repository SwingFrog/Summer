package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TinyRespZip extends TinyResp {

    private final byte[] zipBytes;

    public TinyRespZip(String msg, byte[] zipBytes) {
        super(msg);
        this.zipBytes = zipBytes;
    }

    @Override
    public ByteBuf toByteBuf(String charset) {
        ByteBuf buf = Unpooled.buffer(1 + zipBytes.length);
        buf.writeByte(TinyConst.ORDER_RESP_ZIP);
        buf.writeBytes(zipBytes);
        return buf;
    }

}

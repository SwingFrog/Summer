package com.swingfrog.summer.protocol.tiny.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class TinyRespZip extends TinyResp {

    private final byte[] zipBytes;

    public TinyRespZip(String msg, byte[] zipBytes) {
        super(msg);
        this.zipBytes = zipBytes;
    }

    @Override
    public ByteBuf toByteBuf(ByteBufAllocator alloc, String charset) {
        ByteBuf buf = alloc.directBuffer(1 + zipBytes.length);
        buf.writeByte(TinyConst.ORDER_RESP_ZIP);
        buf.writeBytes(zipBytes);
        return buf;
    }

}

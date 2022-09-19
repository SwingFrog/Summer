package com.swingfrog.summer.protocol.tiny.msg;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.util.ZipUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class TinyPush extends AbstractTiny {

    private final short id;
    private final String msg;

    public static TinyPush ofJSON(short id, Object obj) {
        return new TinyPush(id, JSON.toJSONString(obj));
    }

    public TinyPush(short id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public short getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "TinyPush{" +
                "id=" + id +
                ", msg='" + msg + '\'' +
                '}';
    }

    @Override
    public ByteBuf toByteBuf(ByteBufAllocator alloc, String charset) throws Exception {
        byte[] bytes = msg.getBytes(charset);
        int length = bytes.length;
        if (length > TinyConst.ZIP_USE_THRESHOLD) {
            byte[] zip = ZipUtil.zip(TinyConst.ZIP_ENTRY_NAME, bytes);
            if (zip.length < length) {
                return toByteBuf(alloc, TinyConst.ORDER_PUSH_ZIP, zip);
            }
        }
        return toByteBuf(alloc, TinyConst.ORDER_PUSH_JSON, bytes);
    }

    private ByteBuf toByteBuf(ByteBufAllocator alloc, byte order, byte[] bytes) {
        ByteBuf buf = alloc.directBuffer(3 + bytes.length);
        buf.writeByte(order);
        buf.writeShort(id);
        buf.writeBytes(bytes);
        return buf;
    }

    public TinyPush copy() {
        return new TinyPush(id, msg);
    }

}

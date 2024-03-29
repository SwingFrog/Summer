package com.swingfrog.summer.protocol.tiny.msg;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.util.ZipUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class TinyResp extends AbstractTiny {

    private final String msg;

    public static TinyResp ofJSON(Object obj) {
        return new TinyResp(JSON.toJSONString(obj));
    }

    public TinyResp(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "TinyResp{" +
                "msg='" + msg + '\'' +
                '}';
    }

    @Override
    public ByteBuf toByteBuf(ByteBufAllocator alloc, String charset) throws Exception {
        byte[] bytes = msg.getBytes(charset);
        int length = bytes.length;
        if (length > TinyConst.ZIP_USE_THRESHOLD) {
            byte[] zip = ZipUtil.zip(TinyConst.ZIP_ENTRY_NAME, bytes);
            if (zip.length < length) {
                return toByteBuf(alloc, TinyConst.ORDER_RESP_ZIP, zip);
            }
        }
        return toByteBuf(alloc, TinyConst.ORDER_RESP_JSON, bytes);
    }

    private ByteBuf toByteBuf(ByteBufAllocator alloc, byte order, byte[] bytes) {
        ByteBuf buf = alloc.directBuffer(1 + bytes.length);
        buf.writeByte(order);
        buf.writeBytes(bytes);
        return buf;
    }

}

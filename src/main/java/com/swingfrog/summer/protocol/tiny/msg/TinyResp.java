package com.swingfrog.summer.protocol.tiny.msg;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.util.ZipUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
    public ByteBuf toByteBuf(String charset) throws Exception {
        byte[] bytes = msg.getBytes(charset);
        byte[] zip = ZipUtil.zip(TinyConst.ZIP, bytes);
        if (zip.length < bytes.length) {
            toByteBuf(TinyConst.ORDER_RESP_ZIP, zip);
        }
        return toByteBuf(TinyConst.ORDER_RESP_JSON, bytes);
    }

    private ByteBuf toByteBuf(byte order, byte[] bytes) {
        ByteBuf buf = Unpooled.buffer(1 + bytes.length);
        buf.writeByte(order);
        buf.writeBytes(bytes);
        return buf;
    }

}

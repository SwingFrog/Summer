package com.swingfrog.summer.protocol.tiny.msg;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.util.ZipUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TinyPush extends AbstractTiny {

    private final int id;
    private final String msg;

    public static TinyPush ofJSON(int id, Object obj) {
        return new TinyPush(id, JSON.toJSONString(obj));
    }

    public TinyPush(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public int getId() {
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
    public ByteBuf toByteBuf(String charset) throws Exception {
        byte[] bytes = msg.getBytes(charset);
        byte[] zip = ZipUtil.zip(TinyConst.ZIP, bytes);
        if (zip.length < bytes.length) {
            toByteBuf(TinyConst.ORDER_PUSH_ZIP, zip);
        }
        return toByteBuf(TinyConst.ORDER_PUSH_JSON, bytes);
    }

    private ByteBuf toByteBuf(byte order, byte[] bytes) {
        ByteBuf buf = Unpooled.buffer(5 + bytes.length);
        buf.writeByte(order);
        buf.writeInt(id);
        buf.writeBytes(bytes);
        return buf;
    }

}

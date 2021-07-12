package com.swingfrog.summer.protocol.tiny.msg;

import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.CodeMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TinyError extends AbstractTiny {

    private final int code;
    private final String msg;

    public static TinyError of(int code, String msg) {
        return new TinyError(code, msg);
    }

    public static TinyError of(CodeMsg codeMsg) {
        return new TinyError(codeMsg.getCode(), codeMsg.getMsg());
    }

    public static TinyError of(CodeException codeException) {
        return new TinyError(codeException.getCode(), codeException.getMsg());
    }

    public TinyError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "TinyError{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }

    @Override
    public ByteBuf toByteBuf(String charset) throws Exception {
        byte[] bytes = msg.getBytes(charset);
        ByteBuf buf = Unpooled.buffer(4 + bytes.length);
        buf.writeInt(code);
        buf.writeBytes(bytes);
        return buf;
    }

}

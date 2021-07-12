package com.swingfrog.summer.protocol.tiny;

import com.swingfrog.summer.protocol.tiny.msg.TinyReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class TinyDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final String charset;

    public TinyDecoder(String charset) {
        this.charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws UnsupportedEncodingException {
        short id = msg.readShort();
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        String text = new String(bytes, charset);
        out.add(new TinyReq(id, text));
    }

}

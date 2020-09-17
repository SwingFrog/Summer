package com.swingfrog.summer.protocol.protobuf;

import com.swingfrog.summer.util.PasswordUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ProtobufPasswordDecoder extends MessageToMessageDecoder<ByteBuf> {

    private byte[] pass;

    public ProtobufPasswordDecoder(String password) {
        if (password != null && password.length() > 0) {
            this.pass = password.getBytes();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        int id = msg.readInt();
        byte[] bytes = new byte[msg.readableBytes()];
        if (pass != null) {
            PasswordUtil.convert(pass, bytes);
        }
        msg.readBytes(bytes);
        out.add(Protobuf.of(id, bytes));
    }

}

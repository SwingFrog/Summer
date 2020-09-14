package com.swingfrog.summer.protocol.protobuf;

import com.swingfrog.summer.util.PasswordUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class ProtobufPasswordEncoder extends MessageToMessageEncoder<Protobuf> {

    private byte[] pass;

    public ProtobufPasswordEncoder(String password) {
        if (password != null && password.length() > 0) {
            this.pass = password.getBytes();
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Protobuf msg, List<Object> out) {
        byte[] bytes = msg.getBytes();
        if (pass != null) {
            PasswordUtil.convert(pass, bytes);
        }
        ByteBuf buf = Unpooled.buffer(4 + bytes.length);
        buf.writeInt(msg.getId());
        buf.writeBytes(bytes);
        out.add(buf);
    }

}

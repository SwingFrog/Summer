package com.swingfrog.summer.protocol.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class ProtobufEncoder extends MessageToMessageEncoder<Protobuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Protobuf msg, List<Object> out) {
        byte[] bytes = msg.getBytes();
        ByteBuf buf = ctx.alloc().directBuffer(4 + bytes.length);
        buf.writeInt(msg.getId());
        buf.writeBytes(bytes);
        out.add(buf);
    }

}

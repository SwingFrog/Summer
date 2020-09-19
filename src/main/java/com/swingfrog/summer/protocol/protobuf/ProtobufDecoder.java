package com.swingfrog.summer.protocol.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ProtobufDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        int id = msg.readInt();
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        out.add(Protobuf.of(id, bytes));
    }

}

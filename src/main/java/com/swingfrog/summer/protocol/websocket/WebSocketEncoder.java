package com.swingfrog.summer.protocol.websocket;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class WebSocketEncoder extends MessageToMessageEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
		out.add(new BinaryWebSocketFrame(msg.retain()));
	}

}

package com.swingfrog.summer.protocol.websocket;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class WebSocketDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame>{

	@Override
	protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame data, List<Object> out) {
		out.add(data.content().retain());
	}

}

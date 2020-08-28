package com.swingfrog.summer.server;

import com.swingfrog.summer.config.ServerConfig;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordDecoder;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordEncoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineDecoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineEncoder;
import com.swingfrog.summer.protocol.websocket.WebSocketDecoder;
import com.swingfrog.summer.protocol.websocket.WebSocketEncoder;
import com.swingfrog.summer.protocol.websocket.WebSocketUriFilter;
import com.swingfrog.summer.server.exception.NotFoundProtocolException;
import com.swingfrog.summer.web.WebRequestHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {
	
	private final ServerContext serverContext;

	public ServerInitializer(ServerContext serverContext) {
		this.serverContext = serverContext;
		if (!checkProtocol()) {
			throw new NotFoundProtocolException(serverContext.getConfig().getProtocol());
		}
	}
	
	@Override
	protected void initChannel(SocketChannel sc) {
		ChannelPipeline pipeline = sc.pipeline();
		ServerConfig config = serverContext.getConfig();
		if (ServerConst.SERVER_PROTOCOL_STRING_LINE.equals(config.getProtocol())) {
			pipeline.addLast(new StringPasswordLineDecoder(config.getMsgLength(), config.getCharset(), config.getPassword()));
			pipeline.addLast(new StringPasswordLineEncoder(config.getCharset(), config.getPassword()));
			pipeline.addLast(new ServerStringHandler(serverContext));
		} else if (ServerConst.SERVER_PROTOCOL_WEB_SOCKET.equals(config.getProtocol())) {
			pipeline.addLast(new HttpServerCodec());
			pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
			pipeline.addLast(new ChunkedWriteHandler());
			pipeline.addLast(new WebSocketUriFilter(serverContext));
			pipeline.addLast(new WebSocketServerProtocolHandler("/" + config.getServerName()));
			pipeline.addLast(new WebSocketDecoder());
			pipeline.addLast(new WebSocketEncoder());
			pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
			pipeline.addLast(new LengthFieldPrepender(4));
			pipeline.addLast(new StringPasswordDecoder(config.getCharset(), config.getPassword()));
			pipeline.addLast(new StringPasswordEncoder(config.getCharset(), config.getPassword()));
			pipeline.addLast(new ServerStringHandler(serverContext));
		} else if (ServerConst.SERVER_PROTOCOL_LENGTH_FIELD.equals(config.getProtocol())) {
			pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
			pipeline.addLast(new LengthFieldPrepender(4));
			pipeline.addLast(new StringPasswordDecoder(config.getCharset(), config.getPassword()));
			pipeline.addLast(new StringPasswordEncoder(config.getCharset(), config.getPassword()));
			pipeline.addLast(new ServerStringHandler(serverContext));
		} else if (ServerConst.SERVER_PROTOCOL_HTTP.equals(config.getProtocol())) {
			pipeline.addLast(new HttpServerCodec());
			pipeline.addLast(new HttpObjectAggregator(config.getMsgLength()));
			pipeline.addLast(new ChunkedWriteHandler());
			pipeline.addLast(new WebRequestHandler(serverContext));
		} else {
			throw new NotFoundProtocolException(config.getProtocol());
		}
	}

	private boolean checkProtocol() {
		String protocol = serverContext.getConfig().getProtocol();
		if (ServerConst.SERVER_PROTOCOL_STRING_LINE.equals(protocol)) {
			return true;
		} else if (ServerConst.SERVER_PROTOCOL_WEB_SOCKET.equals(protocol)) {
			return true;
		} else if (ServerConst.SERVER_PROTOCOL_LENGTH_FIELD.equals(protocol)) {
			return true;
		} else return ServerConst.SERVER_PROTOCOL_HTTP.equals(protocol);
	}
}

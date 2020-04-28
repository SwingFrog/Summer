package com.swingfrog.summer.client;

import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordDecoder;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordEncoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineDecoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineEncoder;
import com.swingfrog.summer.server.ServerConst;
import com.swingfrog.summer.server.exception.NotFoundProtocolException;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

	private ClientContext clientContext;

	public ClientInitializer(ClientContext clientContext) {
		this.clientContext = clientContext;
		if (!checkProtocol()) {
			throw new NotFoundProtocolException(clientContext.getConfig().getProtocol());
		}
	}
	
	@Override
	protected void initChannel(SocketChannel sc) {
		ChannelPipeline pipeline = sc.pipeline();
		ClientConfig config = clientContext.getConfig();
		if (ServerConst.SERVER_PROTOCOL_STRING_LINE.equals(config.getProtocol())) {
			pipeline.addLast(new StringPasswordLineDecoder(config.getMsgLength(), config.getCharset(), config.getPassword()));
			pipeline.addLast(new StringPasswordLineEncoder(config.getCharset(), config.getPassword()));
		} else if (ServerConst.SERVER_PROTOCOL_LENGTH_FIELD.equals(config.getProtocol())) {
			pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
			pipeline.addLast(new LengthFieldPrepender(4));
			pipeline.addLast(new StringPasswordDecoder(config.getCharset(), config.getPassword()));
			pipeline.addLast(new StringPasswordEncoder(config.getCharset(), config.getPassword()));
		} else {
			throw new NotFoundProtocolException(config.getProtocol());
		}
		pipeline.addLast(new ClientStringHandler(clientContext));
	}

	private boolean checkProtocol() {
		String protocol = clientContext.getConfig().getProtocol();
		if (ServerConst.SERVER_PROTOCOL_STRING_LINE.equals(protocol)) {
			return true;
		} else return ServerConst.SERVER_PROTOCOL_LENGTH_FIELD.equals(protocol);
	}
}

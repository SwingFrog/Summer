package com.swingfrog.summer.client;

import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordDecoder;
import com.swingfrog.summer.protocol.lengthfield.StringPasswordEncoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineDecoder;
import com.swingfrog.summer.protocol.stringline.StringPasswordLineEncoder;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.server.exception.NotFoundProtocolException;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

	private final ClientContext clientContext;

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
		String protocol = config.getProtocol();
		switch (protocol) {
			case ProtocolConst.SERVER_PROTOCOL_STRING_LINE:
				pipeline.addLast(new StringPasswordLineDecoder(config.getMsgLength(), config.getCharset(), config.getPassword()));
				pipeline.addLast(new StringPasswordLineEncoder(config.getCharset(), config.getPassword()));
				break;
			case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD:
				pipeline.addLast(new LengthFieldBasedFrameDecoder(config.getMsgLength(), 0, 4, 0, 4));
				pipeline.addLast(new LengthFieldPrepender(4));
				pipeline.addLast(new StringPasswordDecoder(config.getCharset(), config.getPassword()));
				pipeline.addLast(new StringPasswordEncoder(config.getCharset(), config.getPassword()));
				break;
			default:
				throw new NotFoundProtocolException(config.getProtocol());
		}
		pipeline.addLast(new ClientStringHandler(clientContext));
	}

	private boolean checkProtocol() {
		String protocol = clientContext.getConfig().getProtocol();
		switch (protocol) {
			case ProtocolConst.SERVER_PROTOCOL_STRING_LINE:
			case ProtocolConst.SERVER_PROTOCOL_LENGTH_FIELD:
				return true;
		}
		return false;
	}
}

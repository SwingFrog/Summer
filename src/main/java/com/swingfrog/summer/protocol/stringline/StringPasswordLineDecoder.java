package com.swingfrog.summer.protocol.stringline;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.swingfrog.summer.util.PasswordUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class StringPasswordLineDecoder extends MessageToMessageDecoder<ByteBuf> {

	private final String charset;
	private byte[] pass;

	public StringPasswordLineDecoder(String charset, String password) {
		this.charset = charset;
		if (password != null && password.length() > 0) {
			try {
				this.pass = password.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				this.pass = password.getBytes();
			}
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		byte[] bytes = new byte[msg.readableBytes()];
		msg.readBytes(bytes);
		if (pass != null) {
			PasswordUtil.convertForLine(pass, bytes);
		}
		out.add(new String(bytes, charset));
	}
	
}

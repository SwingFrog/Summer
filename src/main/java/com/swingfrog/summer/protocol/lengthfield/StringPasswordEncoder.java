package com.swingfrog.summer.protocol.lengthfield;

import java.io.UnsupportedEncodingException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class StringPasswordEncoder extends MessageToMessageEncoder<String> {

	private final String charset;
	private byte[] pass;
	
	public StringPasswordEncoder(String charset, String password) {
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
	protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
		byte[] bytes = msg.getBytes(charset);
		if (pass != null) {
			int index = bytes.length % 10;
			for (int i = 0; i < bytes.length; i++) {
				if (index >= pass.length)
					index = 0;
				int res = bytes[i] ^ pass[index];
				bytes[i] = (byte)res;
				index++;
			}
		}
		ByteBuf buf = Unpooled.buffer(bytes.length);
		buf.writeBytes(bytes);
		out.add(buf);
	}

}

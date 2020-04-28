package com.swingfrog.summer.protocol.lengthfield;

import java.io.UnsupportedEncodingException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class StringPasswordDecoder extends MessageToMessageDecoder<ByteBuf> {

	private String charset;
	private byte[] pass;
	
	public StringPasswordDecoder(String charset, String password) {
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
			int index = bytes.length % 10;
			for (int i = 0; i < bytes.length; i++) {
				if (index >= pass.length)
					index = 0;
				int res = bytes[i] ^ pass[index];
				bytes[i] = (byte)res;
				index++;
			}
		}
		out.add(new String(bytes, charset));
	}

}

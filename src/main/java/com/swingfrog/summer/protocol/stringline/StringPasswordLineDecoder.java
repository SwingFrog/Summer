package com.swingfrog.summer.protocol.stringline;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class StringPasswordLineDecoder extends LineBasedFrameDecoder {
	
	private final String charset;
	private byte[] pass;
	public StringPasswordLineDecoder (int msgLength, String charset, String password) {
		super(msgLength);
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
	protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer)  throws Exception {
		ByteBuf msg = (ByteBuf) super.decode(ctx, buffer);
		if (msg != null) {
			if (pass != null) {
				byte[] bytes = new byte[msg.readableBytes()];
				msg.readBytes(bytes);
				int index = bytes.length % 10;
				for (int i = 0; i < bytes.length; i++) {
					if (index >= pass.length)
						index = 0;
					int res = bytes[i] ^ pass[index];
					if (res != 10 && res != 13)
						bytes[i] = (byte)res;
					index++;
				}
				return new String(bytes, charset);
			} else {
				return msg.toString(Charset.forName(charset));
			}
		}
		return null;
	}
	
}

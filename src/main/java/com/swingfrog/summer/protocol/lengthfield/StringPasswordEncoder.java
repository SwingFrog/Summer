package com.swingfrog.summer.protocol.lengthfield;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.swingfrog.summer.util.PasswordUtil;
import io.netty.buffer.ByteBuf;
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
			PasswordUtil.convert(pass, bytes);
		}
		ByteBuf buf = ctx.alloc().directBuffer(bytes.length);
		buf.writeBytes(bytes);
		out.add(buf);
	}

}

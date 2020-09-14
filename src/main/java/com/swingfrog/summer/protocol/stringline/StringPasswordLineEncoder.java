package com.swingfrog.summer.protocol.stringline;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import com.swingfrog.summer.util.PasswordUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class StringPasswordLineEncoder extends MessageToMessageEncoder<String> {
	
	private final String charset;
	private byte[] pass;
	public StringPasswordLineEncoder (String charset, String password) {
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
		if (msg.length() == 0) 
			return;
		if (pass == null) {
			msg = msg + System.getProperty("line.separator");
			out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), Charset.forName(charset)));
		} else {
			byte[] bytes = msg.getBytes(charset);
			PasswordUtil.convertForLine(pass, bytes);
			ByteBuf buf = Unpooled.buffer(bytes.length+2);
			buf.writeBytes(bytes);
			buf.writeByte('\r');
			buf.writeByte('\n');
			out.add(buf);
		}
	}
}

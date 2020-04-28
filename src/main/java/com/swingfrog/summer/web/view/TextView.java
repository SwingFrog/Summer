package com.swingfrog.summer.web.view;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;

import java.util.Map;

public class TextView implements WebView {

	private ByteBuf byteBuf;
	private volatile Map<String, String> headers;

	public static TextView of(String text) {
		return new TextView(text);
	}
	
	public TextView(String text) {
		byte[] bytes = text.getBytes();
		byteBuf = Unpooled.buffer(bytes.length);
		byteBuf.writeBytes(bytes);
	}
	
	@Override
	public void ready() {
		
	}
	
	@Override
	public int getStatus() {
		return 200;
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}

	@Override
	public long getLength() {
		return byteBuf.readableBytes();
	}

	@Override
	public ChunkedInput<ByteBuf> getChunkedInput() {
		return new ChunkedInput<ByteBuf>() {
			
			@Override
			public boolean isEndOfInput() {
				return true;
			}

			@Override
			public void close() {
				byteBuf.clear();
			}

			@Override
			public ByteBuf readChunk(ChannelHandlerContext ctx) {
				return null;
			}

			@Override
			public ByteBuf readChunk(ByteBufAllocator allocator) {
				return byteBuf;
			}

			@Override
			public long length() {
				return byteBuf.readableBytes();
			}

			@Override
			public long progress() {
				return 0;
			}
			
		};
	}

	@Override
	public String toString() {
		return "TextView";
	}

	public void addHeader(String key, String value) {
		if (headers == null) {
			synchronized (this) {
				if (headers == null) {
					headers = Maps.newConcurrentMap();
				}
			}
		}
		headers.put(key, value);
	}

	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

}

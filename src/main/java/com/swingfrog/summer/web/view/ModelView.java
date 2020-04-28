package com.swingfrog.summer.web.view;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.common.collect.Maps;
import com.swingfrog.summer.web.WebMgr;

import freemarker.template.Template;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;

public class ModelView implements WebView {

	private String view;
	private Map<String, Object> map;
	private ByteBuf byteBuf;
	private volatile Map<String, String> headers;

	public static ModelView of(String view) {
		return new ModelView(view);
	}

	public ModelView(String view) {
		this.view = view;
		map = Maps.newHashMap();
	}

	public ModelView put(String key, Object value) {
		map.put(key, value);
		return this;
	}
	
	@Override
	public void ready() throws Exception {
		Template template = WebMgr.get().getTemplate(view);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		template.process(map, new BufferedWriter(new OutputStreamWriter(out)));
		byteBuf = Unpooled.wrappedBuffer(out.toByteArray());
		out.close();
	}
	
	@Override
	public int getStatus() {
		return 200;
	}

	@Override
	public String getContentType() {
		return "text/html";
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
		return "ModelView";
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

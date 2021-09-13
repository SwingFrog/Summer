package com.swingfrog.summer.web.view;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.common.collect.Maps;
import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.util.ChunkedByteBuf;
import com.swingfrog.summer.web.WebMgr;

import com.swingfrog.summer.web.WebRequest;
import freemarker.template.Template;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.stream.ChunkedInput;

public class ModelView extends AbstractView {

	private final String view;
	private final Map<String, Object> map;

	public static ModelView of(String view) {
		return new ModelView(view);
	}

	public ModelView(String view) {
		this(view, Maps.newHashMap());
	}

	public ModelView(String v, Map<String, Object> m) {
		view = v;
		map = m;
	}

	public ModelView put(String key, Object value) {
		map.put(key, value);
		return this;
	}

	public ModelView putAll(Map<String, Object> m) {
		map.putAll(m);
		return this;
	}
	
	@Override
	public ChunkedInput<ByteBuf> onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception {
		Template template = WebMgr.get().getTemplate(view);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		template.process(map, new BufferedWriter(new OutputStreamWriter(out)));
		ByteBuf byteBuf = Unpooled.wrappedBuffer(out.toByteArray());
		out.close();
		return new ChunkedByteBuf(byteBuf);
	}

	@Override
	public String getContentType() {
		return "text/html";
	}
	
	@Override
	public String toString() {
		return "ModelView";
	}

}

package com.swingfrog.summer.web.view;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.common.collect.Maps;
import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.util.ByteBufOutputStream;
import com.swingfrog.summer.web.WebMgr;

import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.render.DefaultWebViewRender;
import com.swingfrog.summer.web.view.render.WebViewRender;
import freemarker.template.Template;
import io.netty.buffer.ByteBuf;

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
	public WebViewRender onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception {
		Template template = WebMgr.get().getTemplate(view);
		ByteBuf byteBuf = sctx.alloc().directBuffer();
		try (ByteBufOutputStream out = new ByteBufOutputStream(byteBuf)) {
			template.process(map, new BufferedWriter(new OutputStreamWriter(out)));
		}
		return new DefaultWebViewRender(byteBuf);
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

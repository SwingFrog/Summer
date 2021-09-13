package com.swingfrog.summer.web.view;

import java.util.Map;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;
import io.netty.buffer.ByteBuf;
import io.netty.handler.stream.ChunkedInput;

public interface WebView {

	ChunkedInput<ByteBuf> onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception;
	int getStatus();
	String getContentType();
	default Map<String, String> getHeaders() {
		return null;
	}

}

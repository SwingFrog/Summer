package com.swingfrog.summer.web.view;

import java.util.Map;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.render.WebViewRender;

public interface WebView {

	WebViewRender onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception;
	int getStatus();
	String getContentType();
	default Map<String, String> getHeaders() {
		return null;
	}

}

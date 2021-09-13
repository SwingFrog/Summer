package com.swingfrog.summer.web.view;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;

public class TextView extends PlainView {

	protected final String text;

	public static TextView of(String text) {
		return new TextView(text);
	}
	
	public TextView(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "TextView";
	}

	@Override
	protected String getText(ServerContext serverContext, SessionContext sctx, WebRequest request) {
		return text;
	}

}

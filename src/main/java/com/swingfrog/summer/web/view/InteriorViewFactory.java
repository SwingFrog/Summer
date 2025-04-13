package com.swingfrog.summer.web.view;

import com.alibaba.fastjson.JSON;

public class InteriorViewFactory {

	public WebView createBlankView() {
		return new BlankView();
	}
	
	public WebView createErrorView(int status, long code, String msg) {
		return new ErrorView(status, code, msg);
	}
	
	public WebView createErrorView(int status, String msg) {
		return new ErrorView(status, msg);
	}

	public WebView createDefaultView(Object data) {
		return new TextView(JSON.toJSONString(data));
	}
	
}

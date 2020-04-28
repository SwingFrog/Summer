package com.swingfrog.summer.web.view;

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
	
}

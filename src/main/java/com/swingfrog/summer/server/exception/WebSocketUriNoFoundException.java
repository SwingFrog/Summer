package com.swingfrog.summer.server.exception;

public class WebSocketUriNoFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WebSocketUriNoFoundException(String uri) {
		super(String.format("request websocket uri %s not found", uri));
	}
	
}

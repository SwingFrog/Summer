package com.swingfrog.summer.server.exception;

public class NotFoundProtocolException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public NotFoundProtocolException(String msg) {
		super(String.format("not found %s protocol", msg));
	}
	
}

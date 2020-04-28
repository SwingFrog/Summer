package com.swingfrog.summer.server.exception;

public class NotFoundSessionContextException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public NotFoundSessionContextException(String cluster) {
		super(String.format("not found cluster[%s]", cluster));
	}
	
	public NotFoundSessionContextException(String cluster, String serverName) {
		super(String.format("not found cluster[%s] serverName[%s]", cluster, serverName));
	}
}

package com.swingfrog.summer.client.exception;

public class SyncRemoteTimeOutException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public SyncRemoteTimeOutException(String remote, String method) {
		super(String.format("sync invoke remote[%s] method[%s] time out", remote, method));
	}
	
}

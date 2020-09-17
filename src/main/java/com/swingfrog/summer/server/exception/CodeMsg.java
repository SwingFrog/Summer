package com.swingfrog.summer.server.exception;

public class CodeMsg {
	
	private final long code;
	private final String msg;
	
	public CodeMsg(long code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public long getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
}

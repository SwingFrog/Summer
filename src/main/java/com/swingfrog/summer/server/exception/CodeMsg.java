package com.swingfrog.summer.server.exception;

public class CodeMsg {
	
	private final int code;
	private final String msg;
	
	public CodeMsg(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
}

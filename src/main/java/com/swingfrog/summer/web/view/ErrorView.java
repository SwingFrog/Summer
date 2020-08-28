package com.swingfrog.summer.web.view;

public class ErrorView extends TextView {
	
	private final int status;

	public static ErrorView of(int status, long code, String msg) {
		return new ErrorView(status, code, msg);
	}

	public static ErrorView of(int status, String msg) {
		return new ErrorView(status, msg);
	}
	
	public ErrorView(int status, long code, String msg) {
		super(String.format("status: %s, code: %s, msg: %s", status, code, msg));
		this.status = status;
	}
	
	public ErrorView(int status, String msg) {
		super(String.format("status: %s, msg: %s", status, msg));
		this.status = status;
	}
	
	@Override
	public int getStatus() {
		return status;
	}
	
	@Override
	public String toString() {
		return "ErrorView";
	}
}

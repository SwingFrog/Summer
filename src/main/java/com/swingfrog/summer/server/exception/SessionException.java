package com.swingfrog.summer.server.exception;

public class SessionException {

	public static CodeMsg INVOKE_ERROR = new CodeMsg(100, "invoke error");
	public static CodeMsg REMOTE_NOT_EXIST = new CodeMsg(101, "remote not exist");
	public static CodeMsg METHOD_NOT_EXIST = new CodeMsg(102, "method not exist");
	public static CodeMsg PARAMETER_ERROR = new CodeMsg(103, "parameter error");
	public static CodeMsg REMOTE_WAS_PROTECTED = new CodeMsg(104, "remote was protected");
}

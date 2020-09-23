package com.swingfrog.summer.server.exception;

public class SessionException {

	public static final CodeMsg INVOKE_ERROR = new CodeMsg(100, "invoke error");
	public static final CodeMsg REMOTE_NOT_EXIST = new CodeMsg(101, "remote not exist");
	public static final CodeMsg METHOD_NOT_EXIST = new CodeMsg(102, "method not exist");
	public static final CodeMsg PARAMETER_ERROR = new CodeMsg(103, "parameter error");
	public static final CodeMsg REMOTE_WAS_PROTECTED = new CodeMsg(104, "remote was protected");
	public static final CodeMsg PROTOBUF_NOT_EXIST = new CodeMsg(105, "protobuf not exist");

}

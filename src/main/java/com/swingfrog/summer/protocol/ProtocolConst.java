package com.swingfrog.summer.protocol;

public class ProtocolConst {

	// Web
	public static final String SERVER_PROTOCOL_HTTP = "Http";

	// JSON
	public static final String SERVER_PROTOCOL_STRING_LINE = "StringLine";
	public static final String SERVER_PROTOCOL_LENGTH_FIELD = "LengthField";
	public static final String SERVER_PROTOCOL_WEB_SOCKET = "WebSocket";
	public static final String SERVER_PROTOCOL_WEB_SOCKET_STANDARD = "WebSocket-Standard"; // non head length 4 bytes.

	// Protobuf
	public static final String SERVER_PROTOCOL_LENGTH_FIELD_PROTOBUF = "LengthField-Protobuf";
	public static final String SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF = "WebSocket-Protobuf";
	public static final String SERVER_PROTOCOL_WEB_SOCKET_PROTOBUF_STANDARD = "WebSocket-Protobuf-Standard"; // non head length 4 bytes.

	// Tiny JSON
	public static final String SERVER_PROTOCOL_LENGTH_FIELD_TINY = "LengthField-Tiny";
	public static final String SERVER_PROTOCOL_WEB_SOCKET_TINY = "WebSocket-Tiny";
	public static final String SERVER_PROTOCOL_WEB_SOCKET_TINY_STANDARD = "WebSocket-Tiny-Standard"; // non head length 4 bytes.

	public static final String PING = "ping";
	public static final String PONG = "pong";
	public static final String RPC = "rpc";
	public static final String RPC_SPLIT = "\t";

	public static final int PROTOBUF_HEART_BEAT_REQ_ID = 0;
	public static final int PROTOBUF_ERROR_CODE_RESP_ID = 1;

	public static boolean isHttp(String protocol) {
		return protocol.contains("Http");
	}

	public static boolean isProtobuf(String protocol) {
		return protocol.contains("Protobuf");
	}

}

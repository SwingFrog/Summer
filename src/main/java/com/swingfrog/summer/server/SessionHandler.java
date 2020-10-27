package com.swingfrog.summer.server;

import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.struct.AutowireParam;

public interface SessionHandler {

	default int priority() {
		return 0;
	}

	default boolean accept(SessionContext ctx) {
		return true;
	}
	
	default void added(SessionContext ctx) {}

	default void removed(SessionContext ctx) {}
	
	default boolean receive(SessionContext ctx, SessionRequest request) {
		return true;
	}

	// only protobuf
	default boolean receive(SessionContext ctx, ProtobufRequest request) {
		return true;
	}

	default void autowireParam(SessionContext ctx, AutowireParam autowireParam) {}

	default void heartTimeOut(SessionContext ctx) {}

	default void sendTooFastMsg(SessionContext ctx) {}

	default void lengthTooLongMsg(SessionContext ctx) {}

	default void unableParseMsg(SessionContext ctx) {}

	default void repetitionMsg(SessionContext ctx) {}

	default void sending(SessionContext ctx) {}
	
}

package com.swingfrog.summer.server;

import com.swingfrog.summer.protocol.SessionRequest;

public interface SessionHandler {

	boolean accept(SessionContext ctx);
	
	void added(SessionContext ctx);
	
	void removed(SessionContext ctx);
	
	boolean receive(SessionContext ctx, SessionRequest request);
	
	void heartTimeOut(SessionContext ctx);
	
	void sendTooFastMsg(SessionContext ctx);
	
	void lengthTooLongMsg(SessionContext ctx);
	
	void unableParseMsg(SessionContext ctx);
	
	void repetitionMsg(SessionContext ctx);

	void sending(SessionContext ctx);
	
}

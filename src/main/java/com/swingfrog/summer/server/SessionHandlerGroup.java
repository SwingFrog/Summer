package com.swingfrog.summer.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.swingfrog.summer.protocol.SessionRequest;

public class SessionHandlerGroup implements SessionHandler {

	private final List<SessionHandler> sessionHandlerList;
	
	public SessionHandlerGroup() {
		sessionHandlerList = Lists.newLinkedList();
	}
	
	public void addSessionHandler(SessionHandler sessionHandler) {
		sessionHandlerList.add(sessionHandler);
	}
	
	public void removeSessionHandler(SessionHandler sessionHandler) {
		sessionHandlerList.remove(sessionHandler);
	}

	@Override
	public boolean accpet(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			if (!sessionHandler.accpet(ctx)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void added(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.added(ctx);
		}
	}

	@Override
	public void removed(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.removed(ctx);
		}
	}

	@Override
	public boolean receive(SessionContext ctx, SessionRequest request) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			if (!sessionHandler.receive(ctx, request)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void heartTimeOut(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.heartTimeOut(ctx);
		}
	}

	@Override
	public void sendTooFastMsg(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.sendTooFastMsg(ctx);
		}
	}

	@Override
	public void lengthTooLongMsg(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.lengthTooLongMsg(ctx);
		}
	}

	@Override
	public void unableParseMsg(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.unableParseMsg(ctx);
		}
	}

	@Override
	public void repetitionMsg(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.repetitionMsg(ctx);
		}
	}

	@Override
	public void sending(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.sending(ctx);
		}
	}

}

package com.swingfrog.summer.server;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.struct.AutowireParam;

public class SessionHandlerGroup implements SessionHandler {

	private final List<SessionHandler> sessionHandlerList;
	
	public SessionHandlerGroup() {
		sessionHandlerList = Lists.newLinkedList();
	}
	
	public void addSessionHandler(SessionHandler sessionHandler) {
		sessionHandlerList.add(sessionHandler);
		sessionHandlerList.sort(Comparator.comparingInt(SessionHandler::priority).reversed());
	}
	
	public void removeSessionHandler(SessionHandler sessionHandler) {
		sessionHandlerList.remove(sessionHandler);
		sessionHandlerList.sort(Comparator.comparingInt(SessionHandler::priority).reversed());
	}

	@Override
	public boolean accept(SessionContext ctx) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			if (!sessionHandler.accept(ctx)) {
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
	public boolean receive(SessionContext ctx, ProtobufRequest request) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			if (!sessionHandler.receive(ctx, request)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void autowireParam(SessionContext ctx, AutowireParam autowireParam) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.autowireParam(ctx, autowireParam);
		}
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

	@Override
	public void handleReady(SessionContext ctx, SessionRequest request) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.handleReady(ctx, request);
		}
	}

	@Override
	public void handleReady(SessionContext ctx, ProtobufRequest request) {
		for (SessionHandler sessionHandler : sessionHandlerList) {
			sessionHandler.handleReady(ctx, request);
		}
	}

}

package com.swingfrog.summer.concurrent;

import com.swingfrog.summer.server.SessionContext;

import java.util.Objects;

public class SessionQueueMgr extends AbstractTokenQueue {

	private static class SingleCase {
		public static final SessionQueueMgr INSTANCE = new SessionQueueMgr();
	}
	
	private SessionQueueMgr() {

	}
	
	public static SessionQueueMgr get() {
		return SingleCase.INSTANCE;
	}

	public void execute(SessionContext sctx, Runnable runnable) {
		Objects.requireNonNull(sctx);
		Objects.requireNonNull(runnable);
		Object token = sctx.getToken();
		if (token != null) {
			super.execute(token, runnable);
		} else {
			super.execute(sctx.getSessionId(), runnable);
		}
	}

	public void shutdown(SessionContext sctx) {
		Objects.requireNonNull(sctx);
		super.shutdown(sctx.getSessionId());
		Object token = sctx.getToken();
		if (token != null)
			super.shutdown(token);
	}

	public int getQueueSize(SessionContext sctx) {
		Objects.requireNonNull(sctx);
		Object token = sctx.getToken();
		if (token != null) {
			return super.getQueueSize(token);
		} else {
			return super.getQueueSize(sctx.getSessionId());
		}
	}
	
}

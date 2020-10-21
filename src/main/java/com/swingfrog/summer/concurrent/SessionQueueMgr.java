package com.swingfrog.summer.concurrent;

import com.swingfrog.summer.server.SessionContext;

import java.util.Objects;
import java.util.concurrent.Executor;

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

	public void clear(SessionContext sctx) {
		Objects.requireNonNull(sctx);
		super.clear(sctx.getSessionId());
		Object token = sctx.getToken();
		if (token != null)
			super.clear(token);
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

	public Executor getExecutor(SessionContext sctx) {
		Objects.requireNonNull(sctx);
		Object token = sctx.getToken();
		if (token != null) {
			return super.getExecutor(token);
		} else {
			return super.getExecutor(sctx.getSessionId());
		}
	}

	public Executor getExecutorByToken(Object token) {
		Objects.requireNonNull(token);
		return super.getExecutor(token);
	}

}

package com.swingfrog.summer.concurrent;

import com.swingfrog.summer.server.SessionContext;

import java.util.concurrent.Executor;

public class SessionQueueMgr extends AbstractKeyQueue<SessionContext> {

	private static class SingleCase {
		public static final SessionQueueMgr INSTANCE = new SessionQueueMgr();
	}
	
	private SessionQueueMgr() {

	}
	
	public static SessionQueueMgr get() {
		return SingleCase.INSTANCE;
	}

	public void execute(SessionContext sctx, Runnable runnable) {
		super.execute(sctx, runnable);
	}

	public void clear(SessionContext sctx) {
		super.clear(sctx);
	}

	public int getQueueSize(SessionContext sctx) {
		return super.getQueueSize(sctx);
	}

	public Executor getExecutor(SessionContext sctx) {
		return super.getOrCreateQueue(sctx);
	}

}

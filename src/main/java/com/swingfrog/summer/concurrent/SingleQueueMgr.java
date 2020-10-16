package com.swingfrog.summer.concurrent;

import java.util.concurrent.Executor;

public class SingleQueueMgr extends AbstractTokenQueue {

	private static class SingleCase {
		public static final SingleQueueMgr INSTANCE = new SingleQueueMgr();
	}
	
	private SingleQueueMgr() {

	}
	
	public static SingleQueueMgr get() {
		return SingleCase.INSTANCE;
	}

	public void execute(Object key, Runnable runnable) {
		super.execute(key, runnable);
	}

	public void shutdown(Object key) {
		super.shutdown(key);
	}

	public int getQueueSize(Object key) {
		return super.getQueueSize(key);
	}

	public Executor getExecutor(Object key) {
		return super.getExecutor(key);
	}

}

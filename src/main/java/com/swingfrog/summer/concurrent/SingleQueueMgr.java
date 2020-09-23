package com.swingfrog.summer.concurrent;

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

}

package com.swingfrog.summer.concurrent;

import java.util.concurrent.Executor;

public class SessionTokenQueueMgr extends AbstractKeyQueue<Object> {

    private static class SingleCase {
        public static final SessionTokenQueueMgr INSTANCE = new SessionTokenQueueMgr();
    }

    private SessionTokenQueueMgr() {

    }

    public static SessionTokenQueueMgr get() {
        return SessionTokenQueueMgr.SingleCase.INSTANCE;
    }

    public void execute(Object key, Runnable runnable) {
        super.execute(key, runnable);
    }

    public void clear(Object key) {
        super.clear(key);
    }

    public int getQueueSize(Object key) {
        return super.getQueueSize(key);
    }

    public Executor getExecutor(Object key) {
        return super.getOrCreateQueue(key);
    }

}

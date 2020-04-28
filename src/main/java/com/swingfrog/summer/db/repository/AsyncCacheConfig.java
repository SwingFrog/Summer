package com.swingfrog.summer.db.repository;

public class AsyncCacheConfig {

    private int coreThread;

    public int getCoreThread() {
        return coreThread;
    }

    public void setCoreThread(int coreThread) {
        this.coreThread = coreThread;
    }

    @Override
    public String toString() {
        return "AsyncCacheConfig{" +
                "coreThread=" + coreThread +
                '}';
    }
}

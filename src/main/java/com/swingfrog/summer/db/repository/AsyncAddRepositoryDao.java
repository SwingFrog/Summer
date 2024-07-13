package com.swingfrog.summer.db.repository;

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

public abstract class AsyncAddRepositoryDao<T, K> extends RepositoryDao<T, K> {

    private static final Logger log = LoggerFactory.getLogger(AsyncAddRepositoryDao.class);
    private final Queue<T> waitAdd = Queues.newConcurrentLinkedQueue();
    long delayTime;

    protected abstract long delayTime();

    @Override
    void init() {
        super.init();
        delayTime = delayTime();
        AsyncCacheRepositoryMgr.get().getScheduledExecutor().scheduleWithFixedDelay(
                this::delay,
                delayTime,
                delayTime,
                TimeUnit.MILLISECONDS);
        AsyncCacheRepositoryMgr.get().addHook(this::delay);
    }

    private synchronized void delay() {
        if (waitAdd.isEmpty()) {
            return;
        }
        try {
            for (T obj = waitAdd.poll(); obj != null; obj = waitAdd.poll()) {
                super.add(obj);
            }
        } catch (Throwable e) {
            log.error("AsyncAddRepositoryDao delay failure.");
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public T add(T obj) {
        waitAdd.add(obj);
        return obj;
    }

    @Override
    protected boolean isUseReplaceSql() {
        return false;
    }

}

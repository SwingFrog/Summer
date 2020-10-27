package com.swingfrog.summer.concurrent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
public abstract class AbstractKeyQueue<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractKeyQueue.class);

    private volatile boolean active = true;
    private Executor eventExecutor;
    private Cache<Object, RunnableQueue> queueCache;

    public void init(Executor eventExecutor, long queueExpireTimeMs) {
        if (eventExecutor != null && queueCache != null) {
            throw new UnsupportedOperationException("token queue initialized");
        }
        this.eventExecutor = eventExecutor;
        queueCache = CacheBuilder.newBuilder()
                .expireAfterAccess(queueExpireTimeMs, TimeUnit.MILLISECONDS)
                .build();
    }

    protected void execute(T key, Runnable runnable) {
        if (!active) {
            throw new UnsupportedOperationException("token queue is shutdown.");
        }
        getOrCreateQueue(key).execute(runnable);
        log.debug("token queue execute runnable key[{}]", key);
    }

    protected void clear(T key) {
        Objects.requireNonNull(key);
        RunnableQueue rq = queueCache.getIfPresent(key);
        if (rq == null)
            return;
        queueCache.invalidate(key);
        rq.clear();
    }

    protected int getQueueSize(T key) {
        return getOrCreateQueue(key).getSize();
    }

    public boolean isEmpty() {
        for (RunnableQueue runnableQueue : queueCache.asMap().values()) {
            if (!runnableQueue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void shutdown() {
        active = false;
        queueCache.asMap().values().forEach(RunnableQueue::shutdown);
        queueCache.invalidateAll();
    }

    protected RunnableQueue getOrCreateQueue(T key) {
        Objects.requireNonNull(key);
        RunnableQueue rq = queueCache.getIfPresent(key);
        if (rq == null) {
            rq = new RunnableQueue(eventExecutor);
            RunnableQueue old = queueCache.asMap().putIfAbsent(key, rq);
            if (old != null) {
                rq = old;
            }
        }
        return rq;
    }

}

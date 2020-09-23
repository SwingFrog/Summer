package com.swingfrog.summer.concurrent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTokenQueue {

    private static final Logger log = LoggerFactory.getLogger(AbstractTokenQueue.class);

    private ExecutorService eventExecutor;
    private Cache<Object, RunnableQueue> queueCache;

    public void init(ExecutorService eventExecutor, long queueExpireTimeMs) {
        if (eventExecutor != null && queueCache != null) {
            throw new UnsupportedOperationException("token queue initialized");
        }
        this.eventExecutor = eventExecutor;
        queueCache = CacheBuilder.newBuilder()
                .expireAfterAccess(queueExpireTimeMs, TimeUnit.MILLISECONDS)
                .build();
    }

    protected void execute(Object key, Runnable runnable) {
        Objects.requireNonNull(runnable);
        getOrCreateQueue(key).getQueue().add(runnable);
        log.debug("token queue execute runnable key[{}]", key);
        next(key);
    }

    public void shutdown(Object key) {
        Objects.requireNonNull(key);
        RunnableQueue rq = queueCache.getIfPresent(key);
        if (rq == null)
            return;
        queueCache.invalidate(key);
        rq.getQueue().clear();
    }

    public int getQueueSize(Object key) {
        return getOrCreateQueue(key).getQueue().size();
    }

    private RunnableQueue getOrCreateQueue(Object key) {
        Objects.requireNonNull(key);
        RunnableQueue rq = queueCache.getIfPresent(key);
        if (rq == null) {
            rq = RunnableQueue.build();
            RunnableQueue old = queueCache.asMap().putIfAbsent(key, rq);
            if (old != null) {
                rq = old;
            }
        }
        return rq;
    }

    private void next(Object key) {
        RunnableQueue rq = getOrCreateQueue(key);
        AtomicInteger status = rq.getStatus();
        ConcurrentLinkedQueue<Runnable> queue = rq.getQueue();

        do {
            if (status.get() == RunnableQueue.STATUS_RUNNING)
                return;
        } while (!status.compareAndSet(RunnableQueue.STATUS_WAIT, RunnableQueue.STATUS_INTEND));

        Runnable runnable = queue.poll();

        if (runnable == null) {
            status.set(RunnableQueue.STATUS_WAIT);
            return;
        }

        status.set(RunnableQueue.STATUS_RUNNING);
        eventExecutor.execute(()-> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                status.set(RunnableQueue.STATUS_WAIT);
                if (!queue.isEmpty())
                    next(key);
            }
        });
    }

}

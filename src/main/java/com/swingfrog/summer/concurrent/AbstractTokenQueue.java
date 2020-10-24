package com.swingfrog.summer.concurrent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTokenQueue {

    private static final Logger log = LoggerFactory.getLogger(AbstractTokenQueue.class);

    private volatile boolean active = true;
    private Executor eventExecutor;
    private Cache<Object, RunnableQueue> queueCache;
    private Cache<Object, Executor> executorCache;

    public void init(Executor eventExecutor, long queueExpireTimeMs) {
        if (eventExecutor != null && queueCache != null) {
            throw new UnsupportedOperationException("token queue initialized");
        }
        this.eventExecutor = eventExecutor;
        queueCache = CacheBuilder.newBuilder()
                .expireAfterAccess(queueExpireTimeMs, TimeUnit.MILLISECONDS)
                .build();
        executorCache = CacheBuilder.newBuilder()
                .expireAfterAccess(queueExpireTimeMs, TimeUnit.MILLISECONDS)
                .build();
    }

    protected void execute(Object key, Runnable runnable) {
        if (!active) {
            throw new UnsupportedOperationException("token queue is shutdown.");
        }
        Objects.requireNonNull(runnable);
        getOrCreateQueue(key).getQueue().add(runnable);
        log.debug("token queue execute runnable key[{}]", key);
        next(key);
    }

    protected void clear(Object key) {
        Objects.requireNonNull(key);
        RunnableQueue rq = queueCache.getIfPresent(key);
        if (rq == null)
            return;
        queueCache.invalidate(key);
        rq.getQueue().clear();
    }

    protected int getQueueSize(Object key) {
        return getOrCreateQueue(key).getQueue().size();
    }

    protected Executor getExecutor(Object key) {
        Objects.requireNonNull(key);
        Executor executor = executorCache.getIfPresent(key);
        if (executor == null) {
            executor = runnable -> execute(key, runnable);
            Executor old = executorCache.asMap().putIfAbsent(key, executor);
            if (old != null) {
                executor = old;
            }
        }
        return executor;
    }

    public boolean isEmpty() {
        for (RunnableQueue runnableQueue : queueCache.asMap().values()) {
            if (!runnableQueue.getQueue().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void shutdown() {
        active = false;
        try {
            while (!isEmpty()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
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

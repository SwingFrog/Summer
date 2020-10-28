package com.swingfrog.summer.promise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PromisePool {

    private static final Logger log = LoggerFactory.getLogger(PromisePool.class);

    private volatile boolean active = true;
    private final Set<Promise> used = ConcurrentHashMap.newKeySet();
    private final ConcurrentLinkedQueue<Promise> cache = new ConcurrentLinkedQueue<>();
    private final int maxSize;
    private final AtomicInteger promiseId = new AtomicInteger();

    public PromisePool() {
        maxSize = Integer.MAX_VALUE;
    }

    public PromisePool(int size) {
        maxSize = size;
    }

    public Promise createPromise() {
        if (!active) {
            throw new UnsupportedOperationException("promise pool is shutdown.");
        }
        Promise promise = cache.poll();
        if (promise != null) {
            used.add(promise);
            return promise;
        }
        Promise newPromise = new Promise(promiseId.incrementAndGet());
        used.add(newPromise);
        newPromise.setStopHook(() -> {
            used.remove(newPromise);
            if (cache.size() >= maxSize)
                return;
            newPromise.setCatch(null)
                    .setExecutor(null)
                    .clearRunnable();
            cache.add(newPromise);
        });
        return newPromise;
    }

    public int getCacheSize() {
        return cache.size();
    }

    public boolean isCacheEmpty() {
        return cache.isEmpty();
    }

    public int getUsedSize() {
        return used.size();
    }

    public boolean isUsedEmpty() {
        return used.isEmpty();
    }

    public void shutdown() {
        active = false;
        try {
            while (!isUsedEmpty()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

}

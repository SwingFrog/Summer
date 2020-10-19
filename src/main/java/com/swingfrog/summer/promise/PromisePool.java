package com.swingfrog.summer.promise;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PromisePool {

    private final ConcurrentLinkedQueue<Promise> cache = new ConcurrentLinkedQueue<>();
    private final int maxSize;

    public PromisePool() {
        maxSize = Integer.MAX_VALUE;
    }

    public PromisePool(int size) {
        maxSize = size;
    }

    public Promise createPromise() {
        Promise promise = cache.poll();
        if (promise != null)
            return promise;
        Promise newPromise = new Promise();
        newPromise.setStopHook(() -> {
            if (cache.size() >= maxSize)
                return;
            newPromise.setCatch(null)
                    .setExecutor(null)
                    .clearRunnable();
            cache.add(newPromise);
        });
        return newPromise;
    }

    public int getSize() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

}

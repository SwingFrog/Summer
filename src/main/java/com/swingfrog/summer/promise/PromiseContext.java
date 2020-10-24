package com.swingfrog.summer.promise;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PromiseContext {

    private final Promise promise;
    private final AtomicInteger token = new AtomicInteger();
    private final Set<Integer> waitFutures = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<Object, Object> data = new ConcurrentHashMap<>();

    PromiseContext(Promise promise) {
        this.promise = promise;
    }

    void clear() {
        token.set(0);
        waitFutures.clear();
        data.clear();
    }

    boolean hasWaitFuture() {
        return !waitFutures.isEmpty();
    }

    void successFuture(int token) {
        if (waitFutures.remove(token) && waitFutures.isEmpty()) {
            promise.next();
        }
    }

    void failureFuture(int token, Throwable throwable) {
        if (waitFutures.remove(token)) {
            promise.throwError(throwable);
        }
    }

    public void successFuture() {
        if (!waitFutures.isEmpty()) {
            waitFutures.clear();
            promise.next();
        }
    }

    public void failureFuture(Throwable throwable) {
        if (!waitFutures.isEmpty()) {
            waitFutures.clear();
            promise.throwError(throwable);
        }
    }

    public PromiseFuture waitFuture() {
        int newToken = token.getAndIncrement();
        waitFutures.add(newToken);
        return new PromiseFuture(newToken, this);
    }

    public ConcurrentMap<Object, Object> getData() {
        return data;
    }

    public void put(Object key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(Object key) {
        return (T) data.remove(key);
    }

    public void removeAll() {
        data.clear();
    }

}

package com.swingfrog.summer.promise;

public class PromiseFuture {

    private final int token;
    private final PromiseContext context;

    PromiseFuture(int token, PromiseContext context) {
        this.token = token;
        this.context = context;
    }

    public void success() {
        context.successFuture(token);
    }

    public void failure(Throwable throwable) {
        context.failureFuture(token, throwable);
    }

}

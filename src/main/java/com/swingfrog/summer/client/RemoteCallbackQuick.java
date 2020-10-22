package com.swingfrog.summer.client;

public abstract class RemoteCallbackQuick<T> implements RemoteCallback {

    @SuppressWarnings("unchecked")
    @Override
    public final void success(Object obj) {
        successQuick((T) obj);
    }

    protected abstract void successQuick(T data);

}

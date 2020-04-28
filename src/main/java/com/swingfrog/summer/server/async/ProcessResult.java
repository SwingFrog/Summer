package com.swingfrog.summer.server.async;

public class ProcessResult<T> {

    private boolean async;
    private T value;

    public ProcessResult(boolean async, T value) {
        this.async = async;
        this.value = value;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ProcessResult{" +
                "async=" + async +
                ", value=" + value +
                '}';
    }

}

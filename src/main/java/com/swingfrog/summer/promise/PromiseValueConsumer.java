package com.swingfrog.summer.promise;

@FunctionalInterface
public interface PromiseValueConsumer<T> {

    void accept(PromiseContext context, T t);

}

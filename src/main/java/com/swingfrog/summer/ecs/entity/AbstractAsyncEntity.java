package com.swingfrog.summer.ecs.entity;

import java.util.concurrent.Executor;

public abstract class AbstractAsyncEntity<K> extends AbstractEntity<K> {

    public AbstractAsyncEntity(K id) {
        super(id);
    }

    public abstract Executor getExecutor();

}

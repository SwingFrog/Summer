package com.swingfrog.summer.ecs.entity;

public abstract class AbstractAsyncEntity<K> extends AbstractEntity<K> implements AsyncEntity<K> {

    public AbstractAsyncEntity(K id) {
        super(id);
    }

}

package com.swingfrog.summer.ecs.entity;

import com.swingfrog.summer.ecs.bean.Bean;

public abstract class AbstractAsyncBeanEntity<K, B extends Bean<K>> extends AbstractBeanEntity<K, B> implements AsyncEntity<K> {

    public AbstractAsyncBeanEntity(K id) {
        super(id);
    }

}

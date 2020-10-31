package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.entity.Entity;

public abstract class AbstractComponent<K, E extends Entity<K>> implements Component<K, E> {

    private final E entity;

    public AbstractComponent(E entity) {
        this.entity = entity;
    }


    @Override
    public E getEntity() {
        return entity;
    }

}

package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.entity.Entity;

public abstract class AbstractComponent<K, E extends Entity<K>> implements Component {

    private final E entity;

    protected AbstractComponent(E entity) {
        this.entity = entity;
    }

    protected E getEntity() {
        return entity;
    }

}

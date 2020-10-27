package com.swingfrog.summer.ecs.entity.mananger;

import com.swingfrog.summer.ecs.entity.Entity;

public abstract class AbstractGeneralEntityManager<K, E extends Entity<K>> extends AbstractEntityManager<K, E> {

    public E getEntity(K entityId) {
        return super.getEntity(entityId);
    }

}

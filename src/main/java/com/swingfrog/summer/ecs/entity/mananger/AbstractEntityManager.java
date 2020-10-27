package com.swingfrog.summer.ecs.entity.mananger;

import com.swingfrog.summer.ecs.EcsRuntimeException;
import com.swingfrog.summer.ecs.entity.Entity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractEntityManager <K, E extends Entity<K>> {

    private final ConcurrentMap<K, E> cache = new ConcurrentHashMap<>();

    protected E getEntity(K entityId) {
        E entity = cache.get(entityId);
        if (entity == null) {
            entity = loadEntity(entityId);
            if (entity == null)
                throw new EcsRuntimeException("load entity is null -> %s", this.getClass().getName());
            E old = cache.putIfAbsent(entityId, entity);
            if (old != null)
                entity = old;
        }
        return entity;
    }

    public E removeEntity(K entityId) {
        return cache.remove(entityId);
    }

    protected abstract E loadEntity(K entityId);

}

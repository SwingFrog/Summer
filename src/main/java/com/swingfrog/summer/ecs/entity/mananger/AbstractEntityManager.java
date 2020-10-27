package com.swingfrog.summer.ecs.entity.mananger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.swingfrog.summer.ecs.EcsRuntimeException;
import com.swingfrog.summer.ecs.entity.Entity;

import java.util.concurrent.TimeUnit;

public abstract class AbstractEntityManager <K, E extends Entity<K>> {

    private final Cache<K, E> cache;

    protected AbstractEntityManager() {
        long expireTime = expireTime();
        if (expireTime < 0) {
            cache = CacheBuilder.newBuilder().build();
        } else {
            cache = CacheBuilder.newBuilder().expireAfterAccess(expireTime(), TimeUnit.MILLISECONDS).build();
        }
    }

    protected E getEntity(K entityId) {
        E entity = cache.getIfPresent(entityId);
        if (entity == null) {
            entity = loadEntity(entityId);
            if (entity == null)
                throw new EcsRuntimeException("load entity is null -> %s", this.getClass().getName());
            E old = cache.asMap().putIfAbsent(entityId, entity);
            if (old != null)
                entity = old;
        }
        return entity;
    }

    public void removeEntity(K entityId) {
        cache.invalidate(entityId);
    }

    public void removeAllEntity() {
        cache.invalidateAll();
    }

    protected abstract E loadEntity(K entityId);

    protected abstract long expireTime();

}

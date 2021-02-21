package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.db.repository.Repository;
import com.swingfrog.summer.ecs.EcsRuntimeException;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.entity.Entity;

public abstract class AbstractBeanBaseComponent<K, B, E extends Entity<K>> extends AbstractComponent<K, E> {

    final Repository<B, K> repository;

    @SuppressWarnings("unchecked")
    public AbstractBeanBaseComponent(E entity) {
        super(entity);
        BindRepository bindRepository = this.getClass().getAnnotation(BindRepository.class);
        if (bindRepository == null)
            throw new EcsRuntimeException("not found @BindRepository -> %s", this.getClass().getName());
        repository = (Repository<B, K>) Summer.getComponent(bindRepository.value());
    }

}

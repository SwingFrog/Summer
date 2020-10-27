package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.EntityBean;
import com.swingfrog.summer.ecs.entity.Entity;

import java.util.List;

public abstract class AbstractMultiBeanComponent<K, B extends EntityBean<K>, E extends Entity<K>>
        extends AbstractBeanComponent<K, B, E> implements MultiBeanComponent<K, B> {

    private final K entityId;

    protected AbstractMultiBeanComponent(E entity) {
        super(entity);
        entityId = entity.getId();
    }

    @Override
    public List<B> listBean() {
        return repository.listSingleCache(entityId);
    }

    @Override
    public void addBean(B bean) {
        bean.setEntityId(entityId);
        repository.add(bean);
    }

    @Override
    public void removeBean(B bean) {
        if (notEqualEntityId(bean.getEntityId()))
            return;
        repository.remove(bean);
    }

    @Override
    public void removeBeanId(K beanId) {
        B bean = repository.get(beanId);
        if (bean == null)
            return;
        if (notEqualEntityId(bean.getEntityId()))
            return;
        repository.removeByPrimaryKey(beanId);
    }

    @Override
    public void saveBean(B bean) {
        if (notEqualEntityId(bean.getEntityId()))
            return;
        repository.save(bean);
    }

    private boolean notEqualEntityId(K entityId) {
        return this.entityId != entityId;
    }

    protected K getEntityId() {
        return entityId;
    }

}

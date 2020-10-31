package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.entity.Entity;

import java.util.List;

public abstract class AbstractAllBeanComponent<K, B, E extends Entity<K>>
        extends AbstractBeanBaseComponent<K, B, E> implements AllBeanComponent<K, B, E> {

    public AbstractAllBeanComponent(E entity) {
        super(entity);
    }

    @Override
    public List<B> listBean() {
        return repository.list();
    }

    @Override
    public void addBean(B bean) {
        repository.add(bean);
    }

    @Override
    public void removeBean(B bean) {
        repository.remove(bean);
    }

    @Override
    public void removeBeanId(K beanId) {
        repository.removeByPrimaryKey(beanId);
    }

    @Override
    public void removeAllBean() {
        listBean().forEach(repository::remove);
    }

    @Override
    public void saveBean(B bean) {
        repository.save(bean);
    }

}

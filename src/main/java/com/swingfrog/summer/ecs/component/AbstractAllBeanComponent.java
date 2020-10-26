package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.db.repository.Repository;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.bean.Bean;

import java.util.List;

public abstract class AbstractAllBeanComponent<K, B extends Bean<K>> implements AllBeanComponent<K, B> {

    private final Repository<B, K> repository;

    protected AbstractAllBeanComponent() {
        BindRepository bindRepository = this.getClass().getAnnotation(BindRepository.class);
        repository = Summer.getComponent(bindRepository.value());
    }

    @Override
    public List<B> listAllBean() {
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
    public void saveBean(B bean) {
        repository.save(bean);
    }

}

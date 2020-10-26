package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.db.repository.Repository;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.bean.MultiBean;

import java.util.List;

public abstract class AbstractMultiBeanComponent<K, B extends MultiBean<K>> implements MultiBeanComponent<K, B> {

    private final K entityId;
    private final Repository<B, K> repository;

    protected AbstractMultiBeanComponent(K entityId) {
        this.entityId = entityId;
        BindRepository bindRepository = this.getClass().getAnnotation(BindRepository.class);
        repository = Summer.getComponent(bindRepository.value());
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
        checkEntityId(bean.getEntityId());
        repository.remove(bean);
    }

    @Override
    public void removeBeanId(K k) {
        checkEntityId(k);
        repository.removeByPrimaryKey(k);
    }

    @Override
    public void saveBean(B bean) {
        checkEntityId(bean.getEntityId());
        repository.save(bean);
    }

    private void checkEntityId(K entityId) {
        if (this.entityId != entityId) {
            throw new UnsupportedOperationException("entity id error");
        }
    }

}

package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.db.repository.Repository;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.bean.SingleBean;

public abstract class AbstractSingleBeanComponent<K, B extends SingleBean<K>> implements SingleBeanComponent<K, B> {

    private final K entityId;
    private final Repository<B, K> repository;

    protected AbstractSingleBeanComponent(K entityId) {
        this.entityId = entityId;
        BindRepository bindRepository = this.getClass().getAnnotation(BindRepository.class);
        repository = Summer.getComponent(bindRepository.value());
    }

    @Override
    public B getBean() {
        return repository.get(entityId);
    }

    @Override
    public void setBean(B bean) {
        bean.setEntityId(entityId);
        repository.add(bean);
    }

    @Override
    public void removeBean() {
        repository.removeByPrimaryKey(entityId);
    }

    @Override
    public void saveBean(B bean) {
        bean.setEntityId(entityId);
        repository.save(bean);
    }

}

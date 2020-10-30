package com.swingfrog.summer.ecs.entity;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.db.repository.Repository;
import com.swingfrog.summer.ecs.EcsRuntimeException;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.bean.Bean;

public abstract class AbstractBeanEntity<K, B extends Bean<K>> extends AbstractEntity<K> implements BeanEntity<K, B> {

    private final Repository<B, K> repository;
    private B bean;

    public AbstractBeanEntity(K id) {
        super(id);
        BindRepository bindRepository = this.getClass().getAnnotation(BindRepository.class);
        if (bindRepository == null)
            throw new EcsRuntimeException("not found @BindRepository -> %s", this.getClass().getName());
        repository = Summer.getComponent(bindRepository.value());
    }

    @Override
    public B getBean() {
        if (bean == null)
            bean = repository.get(getId());
        return bean;
    }

    @Override
    public B getOrCreate() {
        B bean = getBean();
        if (bean == null) {
            bean = createBean();
            if (bean != null) {
                setBean(bean);
            }
        }
        return bean;
    }

    @Override
    public void setBean(B bean) {
        B old = getBean();
        this.bean = bean;
        if (bean == null) {
            removeBean();
        } else {
            bean.setEntityId(getId());
            if (old == null) {
                repository.add(bean);
            } else {
                saveBean();
            }
        }
    }

    @Override
    public void removeBean() {
        if (getBean() == null)
            return;
        repository.removeByPrimaryKey(getId());
        bean = null;
    }

    @Override
    public void saveBean() {
        if (getBean() == null)
            return;
        repository.forceSave(bean);
    }

    protected B createBean() {
        try {
            return repository.getEntityClass().newInstance();
        } catch (Exception ignored) {

        }
        return null;
    }

}

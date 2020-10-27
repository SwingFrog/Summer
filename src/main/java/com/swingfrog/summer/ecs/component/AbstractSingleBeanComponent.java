package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.EntityBean;
import com.swingfrog.summer.ecs.entity.Entity;

public abstract class AbstractSingleBeanComponent<K, B extends EntityBean<K>, E extends Entity<K>>
        extends AbstractBeanComponent<K, B, E> implements SingleBeanComponent<K, B> {

    private B bean;
    private final K entityId;

    protected AbstractSingleBeanComponent(E entity) {
        super(entity);
        entityId = entity.getId();
    }

    @Override
    public B getBean() {
        if (bean == null)
            bean = repository.get(entityId);
        return bean;
    }

    @Override
    public B getOrCreate() {
        B bean = getBean();
        if (bean == null) {
            bean = createBean();
        }
        if (bean != null) {
            setBean(bean);
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
            bean.setEntityId(entityId);
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
        repository.removeByPrimaryKey(entityId);
    }

    @Override
    public void saveBean() {
        if (getBean() == null)
            return;
        repository.forceSave(bean);
    }

    @Override
    public B createBean() {
        try {
            return repository.getEntityClass().newInstance();
        } catch (Exception ignored) {

        }
        return null;
    }

    protected K getEntityId() {
        return entityId;
    }

}

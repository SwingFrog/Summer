package com.swingfrog.summer.ecs.quick.component;

import com.swingfrog.summer.ecs.bean.EntityBean;
import com.swingfrog.summer.ecs.component.AbstractMultiBeanComponent;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public abstract class QuickMultiBeanComponent<B extends EntityBean<Long>, E extends QuickEntity> extends AbstractMultiBeanComponent<Long, B, E> {

    protected QuickMultiBeanComponent(E entity) {
        super(entity);
    }

}

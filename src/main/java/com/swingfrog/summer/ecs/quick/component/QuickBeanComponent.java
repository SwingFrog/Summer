package com.swingfrog.summer.ecs.quick.component;

import com.swingfrog.summer.ecs.bean.Bean;
import com.swingfrog.summer.ecs.component.AbstractBeanComponent;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public abstract class QuickBeanComponent<B extends Bean<Long>, E extends QuickEntity> extends AbstractBeanComponent<Long, B, E> {

    protected QuickBeanComponent(E entity) {
        super(entity);
    }

}

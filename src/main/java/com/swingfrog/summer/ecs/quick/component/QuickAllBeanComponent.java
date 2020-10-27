package com.swingfrog.summer.ecs.quick.component;

import com.swingfrog.summer.ecs.component.AbstractAllBeanComponent;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public abstract class QuickAllBeanComponent<B, E extends QuickEntity> extends AbstractAllBeanComponent<Long, B, E> {

    protected QuickAllBeanComponent(E entity) {
        super(entity);
    }

}

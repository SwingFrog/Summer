package com.swingfrog.summer.ecs.quick;

import com.swingfrog.summer.ecs.component.AbstractAllBeanComponent;

public abstract class QuickAllBeanComponent<B, E extends QuickEntity> extends AbstractAllBeanComponent<Long, B, E> {

    protected QuickAllBeanComponent(E entity) {
        super(entity);
    }

}

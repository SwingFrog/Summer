package com.swingfrog.summer.ecs.quick.component;

import com.swingfrog.summer.ecs.component.AbstractComponent;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public abstract class QuickComponent<E extends QuickEntity> extends AbstractComponent<Long, E> {

    protected QuickComponent(E entity) {
        super(entity);
    }

}

package com.swingfrog.summer.ecs.quick.component;

import com.swingfrog.summer.ecs.component.AbstractComponent;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public abstract class QuickComponent extends AbstractComponent<Long, QuickEntity> {

    protected QuickComponent(QuickEntity entity) {
        super(entity);
    }

}

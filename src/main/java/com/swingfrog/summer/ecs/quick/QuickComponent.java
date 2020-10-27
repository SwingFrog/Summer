package com.swingfrog.summer.ecs.quick;

import com.swingfrog.summer.ecs.component.AbstractComponent;

public abstract class QuickComponent extends AbstractComponent<Long, QuickEntity> {

    protected QuickComponent(QuickEntity entity) {
        super(entity);
    }

}

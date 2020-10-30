package com.swingfrog.summer.ecs.quick.component;

import com.swingfrog.summer.ecs.bean.Bean;
import com.swingfrog.summer.ecs.component.AbstractMultiBeanComponent;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public abstract class QuickMultiBeanComponent<B extends Bean<Long>, E extends QuickEntity> extends AbstractMultiBeanComponent<Long, B, E> {

    protected QuickMultiBeanComponent(E entity) {
        super(entity);
    }

}

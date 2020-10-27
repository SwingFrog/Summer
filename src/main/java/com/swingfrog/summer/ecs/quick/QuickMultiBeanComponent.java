package com.swingfrog.summer.ecs.quick;

import com.swingfrog.summer.ecs.bean.EntityBean;
import com.swingfrog.summer.ecs.component.AbstractMultiBeanComponent;

public abstract class QuickMultiBeanComponent<B extends EntityBean<Long>, E extends QuickEntity> extends AbstractMultiBeanComponent<Long, B, E> {

    protected QuickMultiBeanComponent(E entity) {
        super(entity);
    }

}

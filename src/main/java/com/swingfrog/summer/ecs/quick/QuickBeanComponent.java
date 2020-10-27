package com.swingfrog.summer.ecs.quick;

import com.swingfrog.summer.ecs.bean.EntityBean;
import com.swingfrog.summer.ecs.component.AbstractSingleBeanComponent;

public abstract class QuickBeanComponent<B extends EntityBean<Long>, E extends QuickEntity> extends AbstractSingleBeanComponent<Long, B, E> {

    protected QuickBeanComponent(E entity) {
        super(entity);
    }

}

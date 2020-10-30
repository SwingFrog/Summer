package com.swingfrog.summer.ecs.quick.entity;

import com.swingfrog.summer.ecs.entity.AbstractBeanEntity;
import com.swingfrog.summer.ecs.quick.bean.QuickBean;

public abstract class QuickBeanEntity<B extends QuickBean> extends AbstractBeanEntity<Long, B> {

    public QuickBeanEntity(Long id) {
        super(id);
    }

}

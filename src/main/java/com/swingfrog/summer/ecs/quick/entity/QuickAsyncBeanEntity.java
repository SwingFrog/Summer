package com.swingfrog.summer.ecs.quick.entity;

import com.swingfrog.summer.ecs.entity.AbstractAsyncBeanEntity;
import com.swingfrog.summer.ecs.quick.bean.QuickBean;

public abstract class QuickAsyncBeanEntity<B extends QuickBean> extends AbstractAsyncBeanEntity<Long, B> {

    public QuickAsyncBeanEntity(Long id) {
        super(id);
    }

}

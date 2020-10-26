package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.SingleBean;

public abstract class AbstractBeanComponent<K, B extends SingleBean<K>> implements BeanComponent<K, B> {

    @Override
    public void add(B bean) {

    }

    @Override
    public void remove(B bean) {

    }

    @Override
    public void save(B bean) {

    }

}

package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.SingleBean;

public interface SingleBeanComponent <K, B extends SingleBean<K>> extends BeanComponent<K, B> {

    B getBean();

    default void setBean(B bean) {
        if (bean == null) {
            remove(getBean());
        } else {
            add(bean);
        }
    }

}

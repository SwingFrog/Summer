package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.EntityBean;

import javax.annotation.Nullable;

public interface SingleBeanComponent <K, B extends EntityBean<K>> extends Component {

    @Nullable
    B getBean();
    B getOrCreate();
    void setBean(B bean);
    void removeBean();
    void saveBean();
    B createBean();

}

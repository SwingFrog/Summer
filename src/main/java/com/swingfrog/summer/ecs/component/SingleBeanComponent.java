package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.EntityBean;
import com.swingfrog.summer.ecs.entity.Entity;

import javax.annotation.Nullable;

public interface SingleBeanComponent <K, B extends EntityBean<K>, E extends Entity<K>> extends Component<K, E> {

    @Nullable
    B getBean();
    B getOrCreate();
    void setBean(B bean);
    void removeBean();
    void saveBean();

}

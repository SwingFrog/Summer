package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.Bean;
import com.swingfrog.summer.ecs.entity.Entity;

import javax.annotation.Nullable;

public interface BeanComponent<K, B extends Bean<K>, E extends Entity<K>> extends Component<K, E> {

    @Nullable
    B getBean();
    B getOrCreate();
    void setBean(B bean);
    void removeBean();
    void saveBean();

}

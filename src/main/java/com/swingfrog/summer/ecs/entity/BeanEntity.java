package com.swingfrog.summer.ecs.entity;

import com.swingfrog.summer.ecs.bean.Bean;

import javax.annotation.Nullable;

public interface BeanEntity<K, B extends Bean<K>> extends Entity<K> {

    @Nullable
    B getBean();
    B getOrCreate();
    void setBean(B bean);
    void removeBean();
    void saveBean();

}

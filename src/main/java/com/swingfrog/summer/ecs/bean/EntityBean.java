package com.swingfrog.summer.ecs.bean;

public interface EntityBean<K> {

    K getEntityId();
    void setEntityId(K k);

}

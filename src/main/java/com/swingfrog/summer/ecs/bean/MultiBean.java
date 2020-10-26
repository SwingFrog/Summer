package com.swingfrog.summer.ecs.bean;

public interface MultiBean<K> extends Bean<K> {

    K getEntityId();
    void setEntityId(K k);
}

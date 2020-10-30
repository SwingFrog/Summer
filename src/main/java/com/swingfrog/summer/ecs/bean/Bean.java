package com.swingfrog.summer.ecs.bean;

public interface Bean<K> {

    K getEntityId();
    void setEntityId(K k);

}

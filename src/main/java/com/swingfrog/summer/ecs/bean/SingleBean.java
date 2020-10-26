package com.swingfrog.summer.ecs.bean;

public interface SingleBean<K> extends Bean<K> {

    default K getEntityId() {
        return getId();
    }

    default void setEntityId(K k) {
        setId(k);
    }

}

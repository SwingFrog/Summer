package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.entity.Entity;

public interface Component<K, E extends Entity<K>> {

    E getEntity();

    default K getEntityId() {
        return getEntity().getId();
    }

}

package com.swingfrog.summer.ecs.entity;

import com.swingfrog.summer.ecs.component.Component;

public interface Entity<K> {

    K getId();
    <C extends Component> C getComponent(Class<C> componentClass);

}

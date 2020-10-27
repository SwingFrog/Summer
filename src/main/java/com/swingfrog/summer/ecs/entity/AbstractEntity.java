package com.swingfrog.summer.ecs.entity;

import com.swingfrog.summer.ecs.EcsRuntimeException;
import com.swingfrog.summer.ecs.component.AbstractComponent;
import com.swingfrog.summer.ecs.component.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractEntity<K> implements Entity<K> {

    private final K id;
    private final ConcurrentMap<Class<? extends Component>, Component> componentMap = new ConcurrentHashMap<>();

    protected AbstractEntity(K id) {
        this.id = id;
    }

    @Override
    public K getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends Component> C getComponent(Class<C> componentClass) {
        Component component = componentMap.get(componentClass);
        if (component == null) {
            try {
                if (componentClass.isAssignableFrom(AbstractComponent.class)) {
                    component = componentClass.getConstructor(this.getClass()).newInstance(this);
                } else {
                    component = componentClass.newInstance();
                }
            } catch (Exception e) {
                throw new EcsRuntimeException("get component failure -> %s", componentClass.getName());
            }
            Component old = componentMap.putIfAbsent(componentClass, component);
            if (old != null) {
                component = old;
            } else {
                component.init();
            }
        }
        return (C) component;
    }

}

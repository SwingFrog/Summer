package com.swingfrog.summer.ecs.entity;

import com.swingfrog.summer.ecs.EcsRuntimeException;
import com.swingfrog.summer.ecs.component.AbstractComponent;
import com.swingfrog.summer.ecs.component.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractEntity<K> implements Entity<K> {

    private final K id;
    private final ConcurrentMap<Class<? extends Component<K, ? extends Entity<K>>>, Component<K, ? extends Entity<K>>> componentMap = new ConcurrentHashMap<>();

    public AbstractEntity(K id) {
        this.id = id;
    }

    @Override
    public K getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    protected <C extends Component<K, ? extends Entity<K>>> C getOrCreateComponent(Class<C> componentClass) {
        Component<K, ? extends Entity<K>> component = componentMap.get(componentClass);
        if (component == null) {
            try {
                if (AbstractComponent.class.isAssignableFrom(componentClass)) {
                    component = componentClass.getConstructor(this.getClass()).newInstance(this);
                } else {
                    component = componentClass.newInstance();
                }
            } catch (Exception e) {
                throw new EcsRuntimeException("get component failure -> %s", componentClass.getName());
            }
            Component<K, ? extends Entity<K>> old = componentMap.putIfAbsent(componentClass, component);
            if (old != null) {
                component = old;
            }
        }
        return (C) component;
    }

}

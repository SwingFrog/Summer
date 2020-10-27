package com.swingfrog.summer.test.ecs.entity;

import com.swingfrog.summer.ecs.component.Component;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public class TestEntity extends QuickEntity {

    public TestEntity(Long id) {
        super(id);
    }

    public <C extends Component<Long, ? extends TestEntity>> C getComponent(Class<C> componentClass) {
        return super.getOrCreateComponent(componentClass);
    }

}

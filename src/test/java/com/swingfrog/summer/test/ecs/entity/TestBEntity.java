package com.swingfrog.summer.test.ecs.entity;

import com.swingfrog.summer.ecs.component.Component;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;

public class TestBEntity extends QuickEntity {

    public TestBEntity(Long id) {
        super(id);
    }

    public <C extends Component<Long, ? extends TestBEntity>> C getComponent(Class<C> componentClass) {
        return super.getOrCreateComponent(componentClass);
    }

}

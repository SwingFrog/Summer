package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.component.Component;
import com.swingfrog.summer.ecs.entity.AbstractAsyncEntity;

import java.util.concurrent.Executor;

public class Player extends AbstractAsyncEntity<Long> {

    public Player(Long id) {
        super(id);
    }

    @Override
    public Executor getExecutor() {
        return Summer.getSessionTokenExecutor(getId());
    }

    public <C extends Component<Long, ? extends Player>> C getComponent(Class<C> componentClass) {
        return super.getOrCreateComponent(componentClass);
    }

}

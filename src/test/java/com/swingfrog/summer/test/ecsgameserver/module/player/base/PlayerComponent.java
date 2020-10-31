package com.swingfrog.summer.test.ecsgameserver.module.player.base;

import com.swingfrog.summer.ecs.component.AbstractComponent;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

public abstract class PlayerComponent extends AbstractComponent<Long, Player> {

    public PlayerComponent(Player entity) {
        super(entity);
    }

}

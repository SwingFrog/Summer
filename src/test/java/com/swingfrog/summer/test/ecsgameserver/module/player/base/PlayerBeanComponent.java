package com.swingfrog.summer.test.ecsgameserver.module.player.base;

import com.swingfrog.summer.ecs.component.AbstractBeanComponent;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

public abstract class PlayerBeanComponent<B extends PlayerBean> extends AbstractBeanComponent<Long, B, Player> {

    public PlayerBeanComponent(Player entity) {
        super(entity);
    }

}

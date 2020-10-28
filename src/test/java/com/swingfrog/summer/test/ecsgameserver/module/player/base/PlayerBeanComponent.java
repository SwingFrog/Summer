package com.swingfrog.summer.test.ecsgameserver.module.player.base;

import com.swingfrog.summer.ecs.component.AbstractSingleBeanComponent;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

public abstract class PlayerBeanComponent<B extends PlayerBean> extends AbstractSingleBeanComponent<Long, B, Player> {

    public PlayerBeanComponent(Player entity) {
        super(entity);
    }

}

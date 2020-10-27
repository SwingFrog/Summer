package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.ecs.component.AbstractSingleBeanComponent;

public abstract class PlayerBeanComponent<B extends PlayerBean> extends AbstractSingleBeanComponent<Long, B, Player> {

    public PlayerBeanComponent(Player entity) {
        super(entity);
    }

}

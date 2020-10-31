package com.swingfrog.summer.test.ecsgameserver.module.team.base;

import com.swingfrog.summer.ecs.component.AbstractBeanComponent;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

public abstract class TeamBeanComponent<B extends TeamBean> extends AbstractBeanComponent<Long, B, Player> {

    public TeamBeanComponent(Player entity) {
        super(entity);
    }

}

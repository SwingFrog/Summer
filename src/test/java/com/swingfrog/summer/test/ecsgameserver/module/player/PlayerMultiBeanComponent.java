package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.ecs.component.AbstractMultiBeanComponent;

public class PlayerMultiBeanComponent<B extends PlayerMultiBean> extends AbstractMultiBeanComponent<Long, B, Player> {

    public PlayerMultiBeanComponent(Player entity) {
        super(entity);
    }

}

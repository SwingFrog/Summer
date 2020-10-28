package com.swingfrog.summer.test.ecsgameserver.module.player.base;

import com.swingfrog.summer.ecs.component.AbstractMultiBeanComponent;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

public class PlayerMultiBeanComponent<B extends PlayerMultiBean> extends AbstractMultiBeanComponent<Long, B, Player> {

    public PlayerMultiBeanComponent(Player entity) {
        super(entity);
    }

}

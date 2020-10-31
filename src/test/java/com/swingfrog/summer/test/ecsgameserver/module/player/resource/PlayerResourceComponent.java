package com.swingfrog.summer.test.ecsgameserver.module.player.resource;

import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerBeanComponent;

@BindRepository(PlayerResourceDao.class)
public class PlayerResourceComponent extends PlayerBeanComponent<PlayerResource> {

    public PlayerResourceComponent(Player entity) {
        super(entity);
    }

}

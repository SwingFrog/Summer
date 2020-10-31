package com.swingfrog.summer.test.ecsgameserver.module.player.item;

import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerMultiBeanComponent;

@BindRepository(PlayerItemDao.class)
public class PlayerItemComponent extends PlayerMultiBeanComponent<PlayerItem> {

    public PlayerItemComponent(Player entity) {
        super(entity);
    }

}

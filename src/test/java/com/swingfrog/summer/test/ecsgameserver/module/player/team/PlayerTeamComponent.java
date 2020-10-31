package com.swingfrog.summer.test.ecsgameserver.module.player.team;

import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerBeanComponent;

@BindRepository(PlayerTeamDao.class)
public class PlayerTeamComponent extends PlayerBeanComponent<PlayerTeam> {

    public PlayerTeamComponent(Player entity) {
        super(entity);
    }

}

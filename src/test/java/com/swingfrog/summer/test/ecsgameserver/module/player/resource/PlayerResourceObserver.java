package com.swingfrog.summer.test.ecsgameserver.module.player.resource;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.BindEvent;
import com.swingfrog.summer.annotation.EventHandler;
import com.swingfrog.summer.test.ecsgameserver.module.login.PlayerLoginEvent;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

@EventHandler
public class PlayerResourceObserver {

    @Autowired
    private PlayerResourceService playerResourceService;

    @BindEvent(PlayerLoginEvent.ID)
    public void onEvent(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        playerResourceService.updateGold(player, 100);
    }

}

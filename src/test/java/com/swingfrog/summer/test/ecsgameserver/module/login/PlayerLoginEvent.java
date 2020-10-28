package com.swingfrog.summer.test.ecsgameserver.module.login;

import com.swingfrog.summer.test.ecsgameserver.module.player.Player;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerEvent;

public class PlayerLoginEvent extends PlayerEvent {

    public static final String ID = PlayerLoginEvent.class.getSimpleName();

    private final long loginTime;

    public PlayerLoginEvent(Player player, long loginTime) {
        super(player);
        this.loginTime = loginTime;
    }

    public long getLoginTime() {
        return loginTime;
    }

}

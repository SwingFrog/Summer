package com.swingfrog.summer.test.ecsgameserver.module.player.base;

import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

public abstract class PlayerEvent {

    protected final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }

}

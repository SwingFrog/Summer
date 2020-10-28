package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.ServerHandler;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.SessionHandler;
import com.swingfrog.summer.struct.AutowireParam;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.SessionHandlerPriority;

@ServerHandler
public class PlayerHandler implements SessionHandler {

    @Autowired
    private PlayerManager playerManager;

    @Override
    public int priority() {
        return SessionHandlerPriority.LOGIN;
    }

    @Override
    public void autowireParam(SessionContext ctx, AutowireParam autowireParam) {
        Long playerId = ctx.getToken();
        if (playerId == null)
            return;
        autowireParam.getTypes().put(Player.class, playerManager.getEntityUnsafe(playerId));
    }

}

package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.annotation.Component;
import com.swingfrog.summer.ecs.entity.mananger.AbstractAsyncEntityManager;

@Component
public class PlayerManager extends AbstractAsyncEntityManager<Long, Player> {

    @Override
    protected Player loadEntity(Long entityId) {
        return new Player(entityId);
    }

    @Override
    protected long expireTime() {
        return 30 * 60 * 1000;
    }

    Player getEntityUnsafe(Long entityId) {
        return super.getEntity(entityId);
    }

}

package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Bean;
import com.swingfrog.summer.ecs.entity.mananger.AbstractAsyncEntityManager;
import com.swingfrog.summer.test.ecsgameserver.module.login.AccountDao;

@Bean
public class PlayerManager extends AbstractAsyncEntityManager<Long, Player> {

    @Autowired
    private AccountDao accountDao;

    @Override
    protected Player loadEntity(Long entityId) {
        return new Player(entityId, accountDao);
    }

    @Override
    protected long expireTime() {
        return 30 * 60 * 1000;
    }

    Player getEntityUnsafe(Long entityId) {
        return super.getEntity(entityId);
    }

}

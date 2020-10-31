package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Component;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.entity.mananger.AbstractAsyncEntityManager;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.ErrorCode;
import com.swingfrog.summer.test.ecsgameserver.module.login.AccountDao;

@Component
public class PlayerManager extends AbstractAsyncEntityManager<Long, Player> {

    @Autowired
    private AccountDao accountDao;

    @Override
    protected Player loadEntity(Long entityId) {
        return new Player(entityId);
    }

    @Override
    protected long expireTime() {
        return 30 * 60 * 1000;
    }

    Player getEntityUnsafe(long playerId) {
        return super.getEntity(playerId);
    }

    public boolean exist(long playerId) {
        return accountDao.get(playerId) != null;
    }

    public void checkExist(long playerId) {
        if (!exist(playerId))
            throw Summer.createCodeException(ErrorCode.PLAYER_NOT_EXIST.getCodeMsg());
    }

}

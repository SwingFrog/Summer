package com.swingfrog.summer.test.ecsgameserver.module.team;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Component;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.entity.mananger.AbstractAsyncEntityManager;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.ErrorCode;

@Component
public class TeamManager extends AbstractAsyncEntityManager<Long, Team> {

    @Autowired
    private TeamDataDao teamDataDao;

    @Override
    protected Team loadEntity(Long entityId) {
        return new Team(entityId);
    }

    @Override
    protected long expireTime() {
        return 30 * 60 * 1000;
    }

    public boolean exist(long teamId) {
        return teamDataDao.get(teamId) != null;
    }

    public void checkExist(long teamId) {
        if (!exist(teamId))
            throw Summer.createCodeException(ErrorCode.TEAM_NOT_EXIST.getCodeMsg());
    }

}

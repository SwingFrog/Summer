package com.swingfrog.summer.test.ecsgameserver.module.team;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.entity.AbstractAsyncBeanEntity;
import com.swingfrog.summer.util.StringUtil;

import java.util.concurrent.Executor;

@BindRepository(TeamDataDao.class)
public class Team extends AbstractAsyncBeanEntity<Long, TeamData> {

    public Team(Long id) {
        super(id);
    }

    @Override
    public Executor getExecutor() {
        return Summer.getExecutor(StringUtil.getString(Team.class.getSimpleName(), getId()));
    }

}

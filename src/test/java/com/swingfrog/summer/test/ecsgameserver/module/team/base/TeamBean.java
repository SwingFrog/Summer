package com.swingfrog.summer.test.ecsgameserver.module.team.base;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.ecs.bean.Bean;

public abstract class TeamBean implements Bean<Long> {

    @PrimaryKey(auto = false)
    @Column
    private long teamId;

    @Override
    public Long getEntityId() {
        return teamId;
    }

    @Override
    public void setEntityId(Long aLong) {
        teamId = aLong;
    }

}

package com.swingfrog.summer.test.ecsgameserver.module.team.base;

import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.ecs.bean.Bean;

public class TeamMultiBean implements Bean<Long> {

    @CacheKey
    @IndexKey
    @Column(readOnly = true)
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

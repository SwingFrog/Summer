package com.swingfrog.summer.test.ecsgameserver.module.player;

import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.ecs.bean.EntityBean;

public class PlayerMultiBean implements EntityBean<Long> {

    @CacheKey
    @IndexKey
    @Column(readOnly = true)
    private long playerId;

    @Override
    public Long getEntityId() {
        return playerId;
    }

    @Override
    public void setEntityId(Long aLong) {
        playerId = aLong;
    }

}

package com.swingfrog.summer.ecs.quick.bean;

import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.ecs.bean.Bean;

public abstract class QuickMultiBean implements Bean<Long> {

    @CacheKey
    @IndexKey
    @Column(readOnly = true)
    private long entityId;

    @Override
    public Long getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(Long aLong) {
        entityId = aLong;
    }

}

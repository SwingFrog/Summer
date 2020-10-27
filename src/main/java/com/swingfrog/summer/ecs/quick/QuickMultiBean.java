package com.swingfrog.summer.ecs.quick;

import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.ecs.bean.EntityBean;

public abstract class QuickMultiBean implements EntityBean<Long> {

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

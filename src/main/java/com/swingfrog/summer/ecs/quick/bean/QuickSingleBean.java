package com.swingfrog.summer.ecs.quick.bean;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.ecs.bean.EntityBean;

public class QuickSingleBean implements EntityBean<Long> {

    @PrimaryKey(auto = false)
    @Column
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

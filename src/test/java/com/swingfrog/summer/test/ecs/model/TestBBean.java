package com.swingfrog.summer.test.ecs.model;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;
import com.swingfrog.summer.ecs.bean.Bean;

@Table(name = "test_b_bean")
public class TestBBean implements Bean<Long> {

    @PrimaryKey(auto = false)
    @Column
    private long id;

    @Column
    private String content;

    @Override
    public Long getEntityId() {
        return id;
    }

    @Override
    public void setEntityId(Long aLong) {
        id = aLong;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

package com.swingfrog.summer.test.repository.model;

import com.swingfrog.summer.db.repository.RepositoryEntity;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;

@Table(name = "t_test_entity", comment = "测试")
public class TestEntity implements RepositoryEntity {

    @PrimaryKey
    @Column(comment = "ID")
    private long id;

    @Column(comment = "值")
    private int value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}

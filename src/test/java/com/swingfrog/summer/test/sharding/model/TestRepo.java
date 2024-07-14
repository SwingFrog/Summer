package com.swingfrog.summer.test.sharding.model;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.ShardingKey;
import com.swingfrog.summer.db.repository.annotation.Table;

import java.util.Date;

@Table(name = "t_test_sharding_repo", comment = "测试-分片")
public class TestRepo {

    @PrimaryKey
    @Column
    private int id;

    @ShardingKey
    @Column(readOnly = true)
    private int type;

    @Column
    private int value;

    @Column
    private Date date;

    public TestRepo() {}

    public TestRepo(int type, int value) {
        this.type = type;
        this.value = value;
        this.date = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TestRepo{" +
                "id=" + id +
                ", type=" + type +
                ", value=" + value +
                ", date=" + date +
                '}';
    }
}

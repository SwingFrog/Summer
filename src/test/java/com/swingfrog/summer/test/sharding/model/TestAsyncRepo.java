package com.swingfrog.summer.test.sharding.model;

import com.swingfrog.summer.db.repository.annotation.*;

import java.util.Date;

@Table(name = "t_test_sharding_async_repo", comment = "测试-分片")
public class TestAsyncRepo {

    @PrimaryKey
    @Column
    private int id;

    @IndexKey
    @CacheKey
    @ShardingKey
    @Column(readOnly = true)
    private int type;

    @Column
    private int value;

    @Column
    private Date date;

    public TestAsyncRepo() {}

    public TestAsyncRepo(int type, int value) {
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
        return "TestAsyncRepo{" +
                "id=" + id +
                ", type=" + type +
                ", value=" + value +
                ", date=" + date +
                '}';
    }
}

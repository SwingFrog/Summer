package com.swingfrog.summer.test.ecsgameserver.login.model;

import com.swingfrog.summer.db.repository.annotation.*;

@Table
public class Account {

    @PrimaryKey
    @Column
    private long id;

    @IndexKey
    @CacheKey
    @Column
    private String openId;

    @Column
    private String name;

    @Column
    private int level;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", openId='" + openId + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                '}';
    }

}

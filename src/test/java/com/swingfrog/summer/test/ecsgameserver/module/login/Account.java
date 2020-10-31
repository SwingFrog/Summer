package com.swingfrog.summer.test.ecsgameserver.module.login;

import com.swingfrog.summer.db.repository.annotation.*;
import com.swingfrog.summer.ecs.bean.Bean;

@Table
public class Account implements Bean<Long> {

    @PrimaryKey
    @Column
    private long id;

    @IndexKey
    @CacheKey
    @Column(readOnly = true)
    private String openId;

    @Column
    private String name;

    @Column
    private long loginTime;

    @Column
    private String loginAddress;

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

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public String getLoginAddress() {
        return loginAddress;
    }

    public void setLoginAddress(String loginAddress) {
        this.loginAddress = loginAddress;
    }

    @Override
    public Long getEntityId() {
        return id;
    }

    @Override
    public void setEntityId(Long aLong) {
        id = aLong;
    }

}

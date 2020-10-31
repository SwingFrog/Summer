package com.swingfrog.summer.test.ecsgameserver.module.team;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.ecs.bean.Bean;

import java.util.Set;

public class TeamData implements Bean<Long> {

    @PrimaryKey
    @Column
    private long id;

    @Column
    private String name;

    @Column
    private Set<Long> memberPlayerIds;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getMemberPlayerIds() {
        return memberPlayerIds;
    }

    public void setMemberPlayerIds(Set<Long> memberPlayerIds) {
        this.memberPlayerIds = memberPlayerIds;
    }

}

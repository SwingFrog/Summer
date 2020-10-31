package com.swingfrog.summer.test.ecsgameserver.module.player.team;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.Table;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerBean;

@Table
public class PlayerTeam extends PlayerBean {

    @Column
    private long teamId;

    @Column
    private long joinTime;

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

}

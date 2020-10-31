package com.swingfrog.summer.test.ecsgameserver.module.team.base;

import com.swingfrog.summer.test.ecsgameserver.module.team.Team;

public abstract class TeamEvent {

    protected final Team team;

    public TeamEvent(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

}

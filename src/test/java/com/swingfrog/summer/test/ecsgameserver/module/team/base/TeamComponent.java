package com.swingfrog.summer.test.ecsgameserver.module.team.base;

import com.swingfrog.summer.ecs.component.AbstractComponent;
import com.swingfrog.summer.test.ecsgameserver.module.team.Team;

public abstract class TeamComponent extends AbstractComponent<Long, Team> {

    public TeamComponent(Team entity) {
        super(entity);
    }

}

package com.swingfrog.summer.test.ecsgameserver.module.team.base;

import com.swingfrog.summer.ecs.component.AbstractMultiBeanComponent;
import com.swingfrog.summer.test.ecsgameserver.module.team.Team;

public class TeamMultiBeanComponent<B extends TeamMultiBean> extends AbstractMultiBeanComponent<Long, B, Team> {

    public TeamMultiBeanComponent(Team entity) {
        super(entity);
    }

}

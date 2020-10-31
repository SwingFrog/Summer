package com.swingfrog.summer.test.ecsgameserver.module.player.team;

import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.ErrorCode;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

@Service
public class PlayerTeamService {

    public void checkNotJoinTeam(Player player) {
        PlayerTeamComponent component = player.getComponent(PlayerTeamComponent.class);
        PlayerTeam bean = component.getBean();
        if (bean == null)
            return;
        if (bean.getTeamId() == 0)
            return;
        throw Summer.createCodeException(ErrorCode.TEAM_JOIN_ALREADY.getCodeMsg());
    }

    public void joinTeam(Player player, long teamId) {
        PlayerTeamComponent component = player.getComponent(PlayerTeamComponent.class);
        PlayerTeam bean = component.getOrCreateBean();
        bean.setTeamId(teamId);
        bean.setJoinTime(System.currentTimeMillis());
        component.saveBean();
    }

}

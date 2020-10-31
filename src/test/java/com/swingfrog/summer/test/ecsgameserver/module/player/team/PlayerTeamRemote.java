package com.swingfrog.summer.test.ecsgameserver.module.player.team;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;
import com.swingfrog.summer.test.ecsgameserver.module.team.TeamManager;

@Remote
public class PlayerTeamRemote {

    @Autowired
    private PlayerTeamService playerTeamService;

    @Autowired
    private TeamManager teamManager;

    public void createTeam(Player player, String teamName) {
        playerTeamService.checkNotJoinTeam(player);
    }

    public void joinTeam(Player player, long teamId) {
        playerTeamService.checkNotJoinTeam(player);
    }

}

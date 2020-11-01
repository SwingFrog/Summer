package com.swingfrog.summer.test.ecsgameserver.module.player.team;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.promise.Promise;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.ErrorCode;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.PromiseManager;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;
import com.swingfrog.summer.test.ecsgameserver.module.player.PlayerManager;
import com.swingfrog.summer.test.ecsgameserver.module.team.TeamData;
import com.swingfrog.summer.test.ecsgameserver.module.team.TeamManager;

import java.util.List;

@Remote
public class PlayerTeamRemote {

    @Autowired
    private PlayerTeamService playerTeamService;

    @Autowired
    private TeamManager teamManager;

    @Autowired
    private PromiseManager promiseManager;

    @Autowired
    private PlayerManager playerManager;

    public void createTeam(Player player, String teamName) {
        playerTeamService.checkNotJoinTeam(player);
        TeamData teamData = new TeamData();
        teamData.setName(teamName);
        teamData.setMemberPlayerIds(Sets.newHashSet(player.getId()));
        teamManager.addTeamData(teamData);
        playerTeamService.joinTeam(player, teamData.getId());
    }

    public AsyncResponse joinTeam(SessionContext ctx, SessionRequest req, Player player, long teamId) {
        playerTeamService.checkNotJoinTeam(player);
        teamManager.checkExist(teamId);
        long playerId = player.getId();
        promiseManager.createPlayerPromise(player, ctx, req)
                .then(teamManager.promiseEntity(teamId, team -> {
                    TeamData teamData = team.getBean();
                    if (teamData == null)
                        throw Summer.createCodeException(ErrorCode.TEAM_NOT_EXIST.getCodeMsg());
                    teamData.getMemberPlayerIds().add(playerId);
                }))
                .then(() -> playerTeamService.joinTeam(player, teamId))
                .then(() -> Summer.asyncResponse(ctx, req))
                .start();
        return AsyncResponse.of();
    }

    public AsyncResponse exitTeam(SessionContext ctx, SessionRequest req, Player player) {
        long teamId = playerTeamService.checkOrGetJoinTeamId(player);
        playerTeamService.exitTeam(player);
        teamManager.checkExist(teamId);
        long playerId = player.getId();
        promiseManager.createPlayerPromise(player, ctx, req)
                .then(teamManager.promiseEntity(teamId, team -> {
                    TeamData teamData = team.getBean();
                    if (teamData == null)
                        return;
                    teamData.getMemberPlayerIds().remove(playerId);
                    if (!teamData.getMemberPlayerIds().isEmpty())
                        return;
                    team.removeBean();
                    teamManager.removeEntity(teamId);
                }))
                .then(() -> Summer.asyncResponse(ctx, req))
                .start();
        return AsyncResponse.of();
    }

    public AsyncResponse listMember(SessionContext ctx, SessionRequest req, Player player) {
        long teamId = playerTeamService.checkOrGetJoinTeamId(player);
        teamManager.checkExist(teamId);
        int RESULT = 0;
        promiseManager.createPlayerPromise(player, ctx, req)
                .then(teamManager.promiseEntity(teamId, (context, team) -> {
                    List<String> list = Lists.newArrayList();
                    context.put(RESULT, list);
                    TeamData teamData = team.getBean();
                    if (teamData == null)
                        return;
                    context.waitFuture();
                    Promise member = promiseManager.createPromise();
                    teamData.getMemberPlayerIds().forEach(memberPlayerId ->
                            member.then(playerManager.promiseEntity(memberPlayerId, memberPlayer ->
                                    list.add(memberPlayer.getName()))));
                    member.then(context::successFuture)
                            .setCatch(context::failureFuture)
                            .start();
                }))
                .then(context -> {
                    List<String> list = context.get(RESULT);
                    Summer.asyncResponse(ctx, req, list);
                })
                .start();
        return AsyncResponse.of();
    }

}

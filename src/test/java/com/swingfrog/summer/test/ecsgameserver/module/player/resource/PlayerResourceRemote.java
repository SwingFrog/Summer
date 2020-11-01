package com.swingfrog.summer.test.ecsgameserver.module.player.resource;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.PromiseManager;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;
import com.swingfrog.summer.test.ecsgameserver.module.player.PlayerManager;

@Remote
public class PlayerResourceRemote {

    @Autowired
    private PlayerResourceService playerResourceService;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private PromiseManager promiseManager;

    public PlayerResource get(Player player) {
        return player.getComponent(PlayerResourceComponent.class).getOrCreateBean();
    }

    public AsyncResponse giveGold(SessionContext ctx, SessionRequest req, Player player, long targetPlayerId, long gold) {
        playerManager.checkExist(targetPlayerId);
        playerResourceService.consumeGold(player, gold);
        int addFriendly = (int) (gold / 10);
        promiseManager.createPlayerPromise(player, ctx, req)
                .then(playerManager.promiseEntity(targetPlayerId, targetPlayer -> playerResourceService.updateGold(player, gold)))
                .then(() -> playerResourceService.updateFriendly(player, addFriendly))
                .then(() -> Summer.asyncResponse(ctx, req))
                .start();
        return AsyncResponse.of();
    }

}

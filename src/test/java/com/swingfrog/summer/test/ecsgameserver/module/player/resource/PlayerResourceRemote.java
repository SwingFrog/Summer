package com.swingfrog.summer.test.ecsgameserver.module.player.resource;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
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
        promiseManager.createPromise()
                .then(playerManager.promiseEntity(targetPlayerId, targetPlayer -> playerResourceService.updateGold(player, gold)))
                .then(() -> playerResourceService.updateFriendly(player, addFriendly))
                .then(() -> Summer.asyncResponse(ctx, req))
                .setExecutor(player.getExecutor())
                .setCatch(throwable -> {
                    if (throwable instanceof CodeException) {
                        CodeException codeException = (CodeException) throwable;
                        Summer.asyncResponse(ctx, req, codeException.getCode(), codeException.getMsg());
                    } else {
                        Summer.asyncResponse(ctx, req, SessionException.INVOKE_ERROR.getCode(), SessionException.INVOKE_ERROR.getMsg());
                    }
                })
                .start();
        return AsyncResponse.of();
    }

}

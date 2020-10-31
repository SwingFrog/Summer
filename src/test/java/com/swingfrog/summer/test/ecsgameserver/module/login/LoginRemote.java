package com.swingfrog.summer.test.ecsgameserver.module.login;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.test.ecsgameserver.module.player.PlayerManager;

import java.util.concurrent.ThreadLocalRandom;

@Remote
public class LoginRemote {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private PlayerManager playerManager;

    public void login(SessionContext sessionContext, String openId) {
        Account account = accountDao.get(openId);
        if (account == null) {
            account = new Account();
            account.setOpenId(openId);
            account.setName("player" + ThreadLocalRandom.current().nextInt(1000));
            accountDao.add(account);
        }
        account.setLoginTime(System.currentTimeMillis());
        account.setLoginAddress(sessionContext.getAddress());
        sessionContext.setToken(account.getId());
        long loginTime = System.currentTimeMillis();
        playerManager.acceptEntity(account.getId(), player -> player.dispatch(PlayerLoginEvent.ID, new PlayerLoginEvent(player, loginTime)));
    }

}

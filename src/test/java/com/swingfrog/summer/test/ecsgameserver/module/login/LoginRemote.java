package com.swingfrog.summer.test.ecsgameserver.module.login;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.server.SessionContext;

import java.util.concurrent.ThreadLocalRandom;

@Remote
public class LoginRemote {

    @Autowired
    private AccountDao accountDao;

    public Account login(SessionContext sessionContext, String openId) {
        Account account = accountDao.get(openId);
        if (account == null) {
            account = new Account();
            account.setOpenId(openId);
            account.setName("player" + ThreadLocalRandom.current().nextInt(1000));
            accountDao.add(account);
        }
        sessionContext.setToken(account.getId());
        return account;
    }

}

package com.swingfrog.summer.test.ecsgameserver.module.login;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.BaseRepository;

import javax.annotation.Nullable;

@Dao
public class AccountDao extends BaseRepository<Account, Long> {

    @Nullable
    public Account get(String openId) {
        return listSingleCache(openId).stream().findAny().orElse(null);
    }

}

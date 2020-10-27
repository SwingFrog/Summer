package com.swingfrog.summer.test.ecsgameserver.login.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.BaseRepository;
import com.swingfrog.summer.test.ecsgameserver.login.model.Account;

@Dao
public class AccountDao extends BaseRepository<Account, Long> {
}

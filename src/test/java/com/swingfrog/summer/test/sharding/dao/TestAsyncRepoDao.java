package com.swingfrog.summer.test.sharding.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.db.repository.AsyncCacheRepositoryDao;
import com.swingfrog.summer.test.sharding.model.TestAsyncRepo;

@Dao
public class TestAsyncRepoDao extends AsyncCacheRepositoryDao<TestAsyncRepo, Integer> {
    @Override
    protected long delayTime() {
        return 1000 * 5;
    }

    @Override
    protected long expireTime() {
        return 1000 * 30;
    }
}

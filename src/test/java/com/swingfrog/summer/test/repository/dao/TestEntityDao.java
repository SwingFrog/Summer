package com.swingfrog.summer.test.repository.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.db.repository.AsyncCacheRepositoryDao;
import com.swingfrog.summer.test.repository.model.TestEntity;

@Dao
public class TestEntityDao extends AsyncCacheRepositoryDao<TestEntity, Long> {

    @Override
    protected long delayTime() {
        return 3000;
    }

    @Override
    protected long expireTime() {
        return 1000000;
    }

}

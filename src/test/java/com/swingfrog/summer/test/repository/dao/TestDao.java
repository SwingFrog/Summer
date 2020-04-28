package com.swingfrog.summer.test.repository.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.db.repository.AsyncCacheRepositoryDao;
import com.swingfrog.summer.test.repository.model.Test;

@Dao
public class TestDao extends AsyncCacheRepositoryDao<Test, Long> {

    @Override
    protected long delayTime() {
        return 300000;
    }

    @Override
    protected long expireTime() {
        return 1000000;
    }

}

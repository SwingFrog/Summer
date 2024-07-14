package com.swingfrog.summer.test.sharding.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.db.repository.AsyncAddRepositoryDao;
import com.swingfrog.summer.test.sharding.model.TestAddRepo;

@Dao
public class TestAddRepoDao extends AsyncAddRepositoryDao<TestAddRepo, Integer> {
    @Override
    protected long delayTime() {
        return 1000 * 5;
    }
}

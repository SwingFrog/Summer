package com.swingfrog.summer.test.sharding.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.db.repository.RepositoryDao;
import com.swingfrog.summer.test.sharding.model.TestRepo;

@Dao
public class TestRepoDao extends RepositoryDao<TestRepo, Integer> {

}

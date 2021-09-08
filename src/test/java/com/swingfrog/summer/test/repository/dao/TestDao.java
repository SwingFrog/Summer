package com.swingfrog.summer.test.repository.dao;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.db.repository.AsyncCacheRepositoryDao;
import com.swingfrog.summer.test.repository.model.Test;
import com.swingfrog.summer.test.repository.service.Test2Service;

@Dao
public class TestDao extends AsyncCacheRepositoryDao<Test, Long> {

    @Autowired
    private Test2Service test2Service;

    @Override
    protected long delayTime() {
        return 300000;
    }

    @Override
    protected long expireTime() {
        return 1000000;
    }

    public void print() {
        test2Service.print();
    }

}

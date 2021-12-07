package com.swingfrog.summer.test.repository.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.db.repository.AsyncAddRepositoryDao;
import com.swingfrog.summer.test.repository.model.TestLog;

@Dao
public class TestLogDao extends AsyncAddRepositoryDao<TestLog, Long> {

    @Override
    protected long delayTime() {
        return 3000;
    }

}

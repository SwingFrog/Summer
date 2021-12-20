package com.swingfrog.summer.test.repository.service;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Component;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.test.repository.dao.TestEntityDao;
import com.swingfrog.summer.test.repository.model.TestEntity;

@Component
public class TestEntityService implements Lifecycle {

    @Autowired
    private TestEntityDao testEntityDao;

    @Override
    public void start() {
        testEntityDao.listAll().forEach(TestEntity::remove);
        TestEntity testEntity = new TestEntity().add();
        testEntity.setValue(123);
        testEntity.save();
        testEntity.remove();
    }

    @Override
    public void stop() {

    }

}

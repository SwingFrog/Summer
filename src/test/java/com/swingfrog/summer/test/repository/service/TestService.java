package com.swingfrog.summer.test.repository.service;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.lifecycle.LifecycleInfo;
import com.swingfrog.summer.test.repository.dao.TestDao;
import com.swingfrog.summer.test.repository.model.Test;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TestService implements Lifecycle {

    @Autowired
    private TestDao testDao;

    @Override
    public LifecycleInfo getInfo() {
        return LifecycleInfo.build("TestService");
    }

    @Override
    public void start() {
        log.info("TestService.start");
        System.out.println(testDao.list("type", 5));
        for (int i = 0; i < 10; i++) {
            testDao.add(Test.of(i, i, "content" + i));
        }
        System.out.println(testDao.list("type", 5));
        testDao.list("type", 5).forEach(testDao::remove);
        testDao.add(Test.of(5, 0, "new"));
        System.out.println(testDao.list("type", 5));
        testDao.list().forEach(testDao::remove);
    }

    @Override
    public void stop() {
        log.info("TestService.stop");
    }

}

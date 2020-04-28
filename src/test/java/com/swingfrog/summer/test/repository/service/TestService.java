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
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                testDao.add(Test.of(0, j, "content" + (i + j)));
            }
        }
        log.info(testDao.get(1L).toString());
        testDao.list("type", 5).forEach(test -> log.info(test.toString()));
        testDao.list().forEach(testDao::remove);
    }

    @Override
    public void stop() {
        log.info("TestService.stop");
    }

}

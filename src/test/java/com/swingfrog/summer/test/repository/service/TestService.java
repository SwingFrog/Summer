package com.swingfrog.summer.test.repository.service;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.lifecycle.LifecycleInfo;
import com.swingfrog.summer.test.repository.dao.TestDao;
import com.swingfrog.summer.test.repository.model.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TestService implements Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

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
            for (int j = 0; j < 3; j++) {
                testDao.removeByPrimaryKey((long) i);
                testDao.add(new Test(i, i, "content" + i));
            }
        }
        System.out.println(testDao.list("type", 5));
        testDao.list("type", 5).forEach(testDao::remove);
        testDao.add(new Test(5, 0, "new"));
        System.out.println(testDao.list("type", 5));
        testDao.removeAll();
    }

    @Override
    public void stop() {
        log.info("TestService.stop");
    }

}

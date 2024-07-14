package com.swingfrog.summer.test.sharding.service;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.test.sharding.dao.TestAsyncRepoDao;
import com.swingfrog.summer.test.sharding.model.TestAsyncRepo;

import java.util.Random;

@Service
public class TestAsyncRepoService implements Lifecycle {

    @Autowired
    private TestAsyncRepoDao testAsyncRepoDao;

    @Override
    public void start() {
        Random random = new Random();
        testAsyncRepoDao.add(new TestAsyncRepo(1, random.nextInt()));
        testAsyncRepoDao.add(new TestAsyncRepo(2, random.nextInt()));
        testAsyncRepoDao.add(new TestAsyncRepo(1, random.nextInt()));

        for (TestAsyncRepo testAsyncRepo : testAsyncRepoDao.list("type", 2)) {
            System.out.println(testAsyncRepo);
        }
        System.out.println(testAsyncRepoDao.listAll().size());
        //testAsyncRepoDao.removeAll();
    }

    @Override
    public void stop() {

    }
}

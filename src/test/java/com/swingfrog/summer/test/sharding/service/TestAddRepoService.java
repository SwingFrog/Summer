package com.swingfrog.summer.test.sharding.service;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.test.sharding.dao.TestAddRepoDao;
import com.swingfrog.summer.test.sharding.model.TestAddRepo;

import java.util.Random;

@Service
public class TestAddRepoService implements Lifecycle {

    @Autowired
    private TestAddRepoDao testAddRepoDao;

    @Override
    public void start() {
        Random random = new Random();
        testAddRepoDao.add(new TestAddRepo(2024, 7, 14, random.nextInt()));
        testAddRepoDao.add(new TestAddRepo(2024, 7, 15, random.nextInt()));
        testAddRepoDao.add(new TestAddRepo(2023, 7, 16, random.nextInt()));
    }

    @Override
    public void stop() {

    }
}

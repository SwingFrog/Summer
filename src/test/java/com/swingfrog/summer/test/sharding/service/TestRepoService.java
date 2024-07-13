package com.swingfrog.summer.test.sharding.service;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.test.sharding.dao.TestRepoDao;
import com.swingfrog.summer.test.sharding.model.TestRepo;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
public class TestRepoService implements Lifecycle {

    @Autowired
    private TestRepoDao testRepoDao;

    @Override
    public void start() {
        Random random = new Random();
        testRepoDao.add(new TestRepo(1, random.nextInt()));
        testRepoDao.add(new TestRepo(2, random.nextInt()));
        testRepoDao.add(new TestRepo(1, random.nextInt()));
        System.out.println("list all");
        List<TestRepo> listAll = testRepoDao.listAll();
        for (TestRepo testRepo : listAll) {
            System.out.println(testRepo);
        }
        System.out.println();
        System.out.println("list type");
        for (TestRepo testRepo : testRepoDao.list("type", 1)) {
            System.out.println(testRepo);
        }

        System.out.println(testRepoDao.get(listAll.stream()
                .max(Comparator.comparingInt(TestRepo::getType)).map(TestRepo::getId).orElse(0)));


    }

    @Override
    public void stop() {

    }
}

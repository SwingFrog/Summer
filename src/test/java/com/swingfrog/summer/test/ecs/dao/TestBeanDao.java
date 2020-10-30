package com.swingfrog.summer.test.ecs.dao;

import com.swingfrog.summer.annotation.Dao;
import com.swingfrog.summer.test.ecs.model.TestBean;

@Dao
public class TestBeanDao extends BaseRepository<TestBean, Long> {
}

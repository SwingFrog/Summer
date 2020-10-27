package com.swingfrog.summer.test.ecs.component;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.quick.component.QuickMultiBeanComponent;
import com.swingfrog.summer.test.ecs.dao.TestMultiBeanDao;
import com.swingfrog.summer.test.ecs.entity.TestEntity;
import com.swingfrog.summer.test.ecs.model.TestMultiBean;

@BindRepository(TestMultiBeanDao.class)
public class TestMultiBeanComponent extends QuickMultiBeanComponent<TestMultiBean, TestEntity> {

    public TestMultiBeanComponent(TestEntity entity) {
        super(entity);
        Summer.autowired(this);
    }

}

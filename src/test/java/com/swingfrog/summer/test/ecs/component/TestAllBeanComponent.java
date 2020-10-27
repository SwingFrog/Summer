package com.swingfrog.summer.test.ecs.component;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.quick.component.QuickAllBeanComponent;
import com.swingfrog.summer.test.ecs.dao.TestAllBeanDao;
import com.swingfrog.summer.test.ecs.entity.TestEntity;
import com.swingfrog.summer.test.ecs.model.TestAllBean;

@BindRepository(TestAllBeanDao.class)
public class TestAllBeanComponent extends QuickAllBeanComponent<TestAllBean, TestEntity> {

    public TestAllBeanComponent(TestEntity entity) {
        super(entity);
        Summer.autowired(this);
    }

}

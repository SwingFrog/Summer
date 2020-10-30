package com.swingfrog.summer.test.ecs.component;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.quick.component.QuickBeanComponent;
import com.swingfrog.summer.test.ecs.config.TestConfig;
import com.swingfrog.summer.test.ecs.dao.TestBeanDao;
import com.swingfrog.summer.test.ecs.entity.TestEntity;
import com.swingfrog.summer.test.ecs.model.TestBean;

@BindRepository(TestBeanDao.class)
public class TestBeanComponent extends QuickBeanComponent<TestBean, TestEntity> {

    @Autowired
    private TestConfig testConfig;

    public TestBeanComponent(TestEntity entity) {
        super(entity);
        Summer.autowired(this);
    }

    @Override
    protected TestBean createBean() {
        TestBean bean = new TestBean();
        bean.setContent(testConfig.getContent());
        return bean;
    }

}

package com.swingfrog.summer.test.ecs.component;

import com.swingfrog.summer.annotation.Autowired;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.quick.component.QuickSingleBeanComponent;
import com.swingfrog.summer.test.ecs.config.TestConfig;
import com.swingfrog.summer.test.ecs.dao.TestSingleBeanDao;
import com.swingfrog.summer.test.ecs.entity.TestEntity;
import com.swingfrog.summer.test.ecs.model.TestSingleBean;

@BindRepository(TestSingleBeanDao.class)
public class TestSingleBeanComponent extends QuickSingleBeanComponent<TestSingleBean, TestEntity> {

    @Autowired
    private TestConfig testConfig;

    public TestSingleBeanComponent(TestEntity entity) {
        super(entity);
        Summer.autowired(this);
    }

    @Override
    protected TestSingleBean createBean() {
        TestSingleBean bean = new TestSingleBean();
        bean.setContent(testConfig.getContent());
        return bean;
    }

}

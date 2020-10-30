package com.swingfrog.summer.test.ecs.entity;

import com.swingfrog.summer.ecs.annotation.BindRepository;
import com.swingfrog.summer.ecs.component.Component;
import com.swingfrog.summer.ecs.entity.AbstractBeanEntity;
import com.swingfrog.summer.ecs.quick.entity.QuickEntity;
import com.swingfrog.summer.test.ecs.dao.TestBBeanDao;
import com.swingfrog.summer.test.ecs.model.TestBBean;

@BindRepository(TestBBeanDao.class)
public class TestBEntity extends AbstractBeanEntity<Long, TestBBean> {

    public TestBEntity(Long id) {
        super(id);
    }

    public <C extends Component<Long, ? extends TestBEntity>> C getComponent(Class<C> componentClass) {
        return super.getOrCreateComponent(componentClass);
    }

    @Override
    protected TestBBean createBean() {
        TestBBean testBBean = new TestBBean();
        testBBean.setId(getId());
        testBBean.setContent("content" + getId());
        return testBBean;
    }
}

package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.EntityBean;

import java.util.List;

public interface MultiBeanComponent <K, B extends EntityBean<K>> extends Component {

    List<B> listBean();
    void addBean(B bean);
    void removeBean(B bean);
    void removeBeanId(K beanId);
    void saveBean(B bean);

}

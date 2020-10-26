package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.Bean;

import java.util.List;

public interface AllBeanComponent <K, B extends Bean<K>> extends Component {

    List<B> listAllBean();
    void addBean(B bean);
    void removeBean(B bean);
    void removeBeanId(K beanId);
    void saveBean(B bean);

}

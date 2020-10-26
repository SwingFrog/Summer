package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.MultiBean;

import java.util.List;

public interface MultiBeanComponent <K, B extends MultiBean<K>> extends Component {

    List<B> listBean();
    void addBean(B bean);
    void removeBean(B bean);
    void removeBeanId(K k);
    void saveBean(B bean);

}

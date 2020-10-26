package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.MultiBean;

import java.util.List;

public interface MultiBeanComponent <K, B extends MultiBean<K>> extends BeanComponent<K, B> {

    List<B> listBean();

}

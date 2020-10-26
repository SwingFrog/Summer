package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.Bean;

import java.util.List;

public interface AllBeanComponent <K, B extends Bean<K>> extends BeanComponent<K, B> {

    List<B> listAll();

}

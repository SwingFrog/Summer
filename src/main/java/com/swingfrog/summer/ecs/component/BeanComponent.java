package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.Bean;

public interface BeanComponent<K, B extends Bean<K>> extends Component {

    void add(B bean);
    void remove(B bean);
    void save(B bean);

}

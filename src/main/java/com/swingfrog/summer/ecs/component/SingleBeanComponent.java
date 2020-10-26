package com.swingfrog.summer.ecs.component;

import com.swingfrog.summer.ecs.bean.SingleBean;

public interface SingleBeanComponent <K, B extends SingleBean<K>> extends Component {

    B getBean();
    void setBean(B bean);
    void removeBean();
    void saveBean(B bean);

}

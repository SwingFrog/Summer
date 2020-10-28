package com.swingfrog.summer.test.ecsgameserver.infrastructure;

import com.swingfrog.summer.annotation.Bean;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.lifecycle.LifecycleInfo;
import com.swingfrog.summer.promise.PromisePool;

@Bean
public class PromiseManager extends PromisePool implements Lifecycle {

    @Override
    public LifecycleInfo getInfo() {
        return LifecycleInfo.build("PromiseManager");
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        shutdown();
    }

}

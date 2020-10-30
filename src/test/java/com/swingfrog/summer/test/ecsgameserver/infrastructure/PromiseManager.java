package com.swingfrog.summer.test.ecsgameserver.infrastructure;

import com.swingfrog.summer.annotation.Component;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.lifecycle.LifecycleInfo;
import com.swingfrog.summer.promise.PromisePool;

@Component
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

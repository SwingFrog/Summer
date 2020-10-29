package com.swingfrog.summer.test.server.slave.service;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.annotation.Service;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.client.ClientRemote;
import com.swingfrog.summer.client.RemoteCallback;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.lifecycle.LifecycleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Service
public class TestService implements Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    @Override
    public LifecycleInfo getInfo() {
        return LifecycleInfo.build("TestService");
    }

    @Override
    public void start() {
        ClientRemote clientRemote = Summer.getRandomClientRemote("Master");
        clientRemote.retryAsyncRemote("MasterRemote", "asyncMul", ImmutableMap.of("a", 3, "b", 3), new RemoteCallback() {
            @Override
            public void success(Object obj) {
                log.info("result from retry async master -> {}", obj);
            }
            @Override
            public void failure(long code, String msg) {
                log.error(msg);
            }
        }, 1, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {

    }

}

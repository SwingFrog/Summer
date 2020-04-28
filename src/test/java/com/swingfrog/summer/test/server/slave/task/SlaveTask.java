package com.swingfrog.summer.test.server.slave.task;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.annotation.IntervalTask;
import com.swingfrog.summer.annotation.Task;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.client.ClientRemote;
import com.swingfrog.summer.client.RemoteCallback;
import com.swingfrog.summer.test.server.master.remote.MasterRemote;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Task
public class SlaveTask {

    @IntervalTask(3000)
    public void sendToMaster() {
        MasterRemote masterRemote = Summer.getRandomRemoteInvokeObjectWithRetry("Master", MasterRemote.class);
        log.info("result from master -> {}", masterRemote.add(1, 2));
    }

    @IntervalTask(5000)
    public void asyncReqMaster() {
        ClientRemote clientRemote = Summer.getRandomClientRemote("Master");
        clientRemote.asyncRemote("MasterRemote", "asyncMul", ImmutableMap.of("a", 3, "b", 3), new RemoteCallback() {
            @Override
            public void success(Object obj) {
                log.info("result from async master -> {}", obj);
            }
            @Override
            public void failure(long code, String msg) {
                log.error(msg);
            }
        });
    }

}

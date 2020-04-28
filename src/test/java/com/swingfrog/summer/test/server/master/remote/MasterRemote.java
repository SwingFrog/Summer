package com.swingfrog.summer.test.server.master.remote;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.annotation.SessionQueue;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.async.AsyncResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Remote
public class MasterRemote {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @SessionQueue
    public int add(int a, int b) {
        log.info("recv from slave a[{}] b[{}]", a, b);
        Summer.getServerPush().asyncPushToClusterRandomServer("Slave", "SlavePush", "recv", ImmutableMap.of("msg", "hello"));
        return a + b;
    }

    @SessionQueue
    public AsyncResponse asyncMul(SessionContext sctx, SessionRequest request, int a, int b) {
        log.info("recv from slave a[{}] b[{}]", a, b);
        executor.execute(() -> sctx.send(request, a * b));
        return AsyncResponse.of();
    }

}

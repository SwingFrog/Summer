package com.swingfrog.summer.test.server.master.remote;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.async.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Remote
public class MasterRemote {

    private static final Logger log = LoggerFactory.getLogger(MasterRemote.class);

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public int add(int a, int b) {
        log.info("recv from slave a[{}] b[{}]", a, b);
        Summer.getServerPush().asyncPushToClusterRandomServer("Slave", "SlavePush", "recv", ImmutableMap.of("msg", "hello"));
        return a + b;
    }

    public AsyncResponse asyncMul(SessionContext sctx, SessionRequest request, int a, int b) {
        log.info("recv from slave a[{}] b[{}]", a, b);
        executor.execute(() -> Summer.asyncResponse(sctx, request, () -> a * b));
        return AsyncResponse.of();
    }

}

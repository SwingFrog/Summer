package com.swingfrog.summer.test.tiny;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import com.swingfrog.summer.server.RemoteTinyDispatchMgr;
import com.swingfrog.summer.test.protobuf.TestProtobufBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTinyBootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(TestProtobufBootstrap.class);

    @Override
    public void init() {
        log.info("init");
        RemoteTinyDispatchMgr tiny = RemoteTinyDispatchMgr.get();
        tiny.addRemote((short) 1, "TestRemote", "hello");
        tiny.addRemote((short) 2, "TestRemote", "blank");
        tiny.addRemote((short) 3, "TestRemote", "error");
        tiny.addRemote((short) 10001, "TestPush", "notice");
    }

    @Override
    public void start() {
        log.info("start");
    }

    @Override
    public void stop() {
        log.info("stop");
    }

    public static void main(String[] args) {
        String resources = TestTinyBootstrap.class.getClassLoader().getResource("tiny").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestTinyBootstrap())
                .dbProperties(resources + "/db.properties")
                .redisProperties(resources + "/redis.properties")
                .serverProperties(resources + "/server.properties")
                .taskProperties(resources + "/task.properties")
                .build());
    }

}

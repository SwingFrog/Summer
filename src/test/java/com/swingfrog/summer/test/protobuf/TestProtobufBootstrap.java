package com.swingfrog.summer.test.protobuf;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import com.swingfrog.summer.test.repository.TestRepositoryBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProtobufBootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(TestProtobufBootstrap.class);

    @Override
    public void init() {
        log.info("init");
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
        String resources = TestProtobufBootstrap.class.getClassLoader().getResource("protobuf").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestRepositoryBootstrap())
                .dbProperties(resources + "/db.properties")
                .redisProperties(resources + "/redis.properties")
                .serverProperties(resources + "/server.properties")
                .taskProperties(resources + "/task.properties")
                .build());
    }

}

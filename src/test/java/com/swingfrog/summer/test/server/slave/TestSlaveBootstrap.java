package com.swingfrog.summer.test.server.slave;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSlaveBootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(TestSlaveBootstrap.class);

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
        Summer.addModuleNet();
        String resources = TestSlaveBootstrap.class.getClassLoader().getResource("server/slave").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestSlaveBootstrap())
                .dbProperties(resources + "/db.properties")
                .redisProperties(resources + "/redis.properties")
                .serverProperties(resources + "/server.properties")
                .taskProperties(resources + "/task.properties")
                .build());
    }

}

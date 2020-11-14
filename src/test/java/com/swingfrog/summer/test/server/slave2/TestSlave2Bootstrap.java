package com.swingfrog.summer.test.server.slave2;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSlave2Bootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(TestSlave2Bootstrap.class);

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
        String resources = TestSlave2Bootstrap.class.getClassLoader().getResource("server/slave2").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestSlave2Bootstrap())
                .dbProperties(resources + "/db.properties")
                .redisProperties(resources + "/redis.properties")
                .serverProperties(resources + "/server.properties")
                .taskProperties(resources + "/task.properties")
                .build());
    }

}

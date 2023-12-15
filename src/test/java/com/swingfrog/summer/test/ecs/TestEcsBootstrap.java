package com.swingfrog.summer.test.ecs;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEcsBootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(TestEcsBootstrap.class);

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
        Summer.addModuleDb();
        String resources = TestEcsBootstrap.class.getClassLoader().getResource("ecs").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestEcsBootstrap())
                .dbProperties(resources + "/db.properties")
                .redisProperties(resources + "/redis.properties")
                .serverProperties(resources + "/server.properties")
                .taskProperties(resources + "/task.properties")
                .build());

        // 请先创建数据库
    }

}

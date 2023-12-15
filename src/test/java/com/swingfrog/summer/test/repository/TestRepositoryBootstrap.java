package com.swingfrog.summer.test.repository;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRepositoryBootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(TestRepositoryBootstrap.class);

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
        Summer.addModuleDb();
        String resources = TestRepositoryBootstrap.class.getClassLoader().getResource("repository").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestRepositoryBootstrap())
                .dbProperties(resources + "/db.properties")
                .redisProperties(resources + "/redis.properties")
                .serverProperties(resources + "/server.properties")
                .taskProperties(resources + "/task.properties")
                .build());

        // 请先创建数据库
    }
}

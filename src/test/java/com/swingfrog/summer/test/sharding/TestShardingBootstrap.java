package com.swingfrog.summer.test.sharding;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;

public class TestShardingBootstrap implements SummerApp {

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public static void main(String[] args) {
        Summer.addModuleDb();
        String resources = TestShardingBootstrap.class.getClassLoader().getResource("sharding").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestShardingBootstrap())
                .dbProperties(resources + "/db.properties")
                .build());
    }
}

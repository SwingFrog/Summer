package com.swingfrog.summer.test.quick;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickBootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(QuickBootstrap.class);

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
        Summer.hot(new QuickBootstrap()); // 不使用外部配置，通过内部默认配置启动
    }

}

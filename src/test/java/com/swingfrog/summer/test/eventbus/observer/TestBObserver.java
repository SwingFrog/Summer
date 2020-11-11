package com.swingfrog.summer.test.eventbus.observer;

import com.swingfrog.summer.annotation.BindEvent;
import com.swingfrog.summer.annotation.EventHandler;
import com.swingfrog.summer.test.eventbus.event.TestClassAEvent;
import com.swingfrog.summer.test.eventbus.event.TestClassBEvent;
import com.swingfrog.summer.test.eventbus.event.TestNameAEvent;
import com.swingfrog.summer.test.eventbus.event.TestNameBEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventHandler
public class TestBObserver {

    private static final Logger log = LoggerFactory.getLogger(TestBObserver.class);

    public void onEvent(TestClassBEvent event) {
        log.info("TestClassBEvent");
    }

    @BindEvent(TestNameBEvent.ID)
    public void onEvent(TestNameBEvent event) {
        log.info("TestNameBEvent");
    }

    private void onEvent(TestClassAEvent event) {
        log.info("TestClassAEvent!!!");
    }

    @BindEvent(TestNameAEvent.ID)
    private void onEvent(TestNameAEvent event) {
        log.info("TestNameAEvent!!!");
    }

}

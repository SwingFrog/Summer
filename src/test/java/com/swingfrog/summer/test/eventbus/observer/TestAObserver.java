package com.swingfrog.summer.test.eventbus.observer;

import com.swingfrog.summer.annotation.AcceptEvent;
import com.swingfrog.summer.annotation.BindEvent;
import com.swingfrog.summer.annotation.EventHandler;
import com.swingfrog.summer.test.eventbus.event.TestClassAEvent;
import com.swingfrog.summer.test.eventbus.event.TestNameAEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventHandler
public class TestAObserver {

    private static final Logger log = LoggerFactory.getLogger(TestAObserver.class);

    @AcceptEvent
    public void onEvent(TestClassAEvent event) {
        log.info("TestClassAEvent");
    }

    @BindEvent(TestNameAEvent.ID)
    public void onEvent(TestNameAEvent event) {
        log.info("TestNameAEvent");
    }

}

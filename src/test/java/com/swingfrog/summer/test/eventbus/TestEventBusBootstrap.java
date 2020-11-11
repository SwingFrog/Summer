package com.swingfrog.summer.test.eventbus;

import com.swingfrog.summer.annotation.Component;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.event.EventBusMgr;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.test.eventbus.event.TestClassAEvent;
import com.swingfrog.summer.test.eventbus.event.TestClassBEvent;
import com.swingfrog.summer.test.eventbus.event.TestNameAEvent;
import com.swingfrog.summer.test.eventbus.event.TestNameBEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

@Component
public class TestEventBusBootstrap implements Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(TestEventBusBootstrap.class);

    @Override
    public void start() {
        log.info("start");
        Summer.syncDispatch(new TestClassAEvent());
        Summer.syncDispatch(new TestClassBEvent());
        Summer.syncDispatch(TestNameAEvent.ID, new TestNameAEvent());
        Summer.syncDispatch(TestNameBEvent.ID, new TestNameBEvent());
    }

    @Override
    public void stop() {

    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        String projectPackage = TestEventBusBootstrap.class.getPackage().getName();
        ContainerMgr.get().init(projectPackage);
        EventBusMgr.get().init();
        ContainerMgr.get().autowired();
        ContainerMgr.get().listDeclaredComponent(Lifecycle.class).stream()
                .filter(l -> l.getInfo() != null)
                .sorted(Comparator.comparingInt(l -> -l.getInfo().getPriority()))
                .forEach(Lifecycle::start);
    }

}

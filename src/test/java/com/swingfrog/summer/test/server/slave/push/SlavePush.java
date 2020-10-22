package com.swingfrog.summer.test.server.slave.push;

import com.swingfrog.summer.annotation.Push;
import com.swingfrog.summer.app.Summer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Push
public class SlavePush {

    private static final Logger log = LoggerFactory.getLogger(SlavePush.class);

    public void recv(String msg) {
        log.info("msg from master push -> {}", msg);
        Summer.getServerPush().syncPushToAll("SlavePush", "msg", msg);
    }

}

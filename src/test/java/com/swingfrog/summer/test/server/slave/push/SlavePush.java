package com.swingfrog.summer.test.server.slave.push;

import com.swingfrog.summer.annotation.Push;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Push
public class SlavePush {

    public void recv(String msg) {
        log.info("msg from master push -> {}", msg);
    }

}

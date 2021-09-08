package com.swingfrog.summer.test.repository.service;

import com.swingfrog.summer.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Test2Service {

    private static final Logger log = LoggerFactory.getLogger(Test2Service.class);

    public void print() {
        log.info("Test2Service.print");
    }

}

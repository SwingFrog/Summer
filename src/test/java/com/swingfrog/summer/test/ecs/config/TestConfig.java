package com.swingfrog.summer.test.ecs.config;

import com.swingfrog.summer.annotation.Bean;

@Bean
public class TestConfig {

    public String getContent() {
        return "new content";
    }

}

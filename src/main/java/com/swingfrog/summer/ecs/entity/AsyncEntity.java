package com.swingfrog.summer.ecs.entity;

import java.util.concurrent.Executor;

public interface AsyncEntity<K> extends Entity<K> {

    Executor getExecutor();

}

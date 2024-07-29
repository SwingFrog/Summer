package com.swingfrog.summer.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ClientReconnectExeMgr {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private static class SingleCase {
        public static final ClientReconnectExeMgr INSTANCE = new ClientReconnectExeMgr();
    }

    private ClientReconnectExeMgr() {}

    public static ClientReconnectExeMgr get() {
        return SingleCase.INSTANCE;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

}

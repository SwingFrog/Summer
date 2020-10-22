package com.swingfrog.summer.meter;

import io.netty.channel.EventLoopGroup;

import java.net.URI;

public interface MeterClient {

    void syncConnect(URI uri, EventLoopGroup group) throws Exception;
    void sendHeartBeat();
    void close();
    boolean isActive();
    void init();

}

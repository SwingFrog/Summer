package com.swingfrog.summer.meter;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MeterClientGroup {

    private URI uri;
    private int threadCount;
    private int startId;
    private int endId;
    private long heartBeatTime;

    public static MeterClientGroup create() {
        return new MeterClientGroup();
    }

    public MeterClientGroup uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public MeterClientGroup threadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public MeterClientGroup startId(int startId) {
        this.startId = startId;
        return this;
    }

    public MeterClientGroup endId(int endId) {
        this.endId = endId;
        return this;
    }

    public MeterClientGroup rangeClosed(int startId, int endId) {
        this.startId = startId;
        this.endId = endId;
        return this;
    }

    public MeterClientGroup hearBeatTime(long heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
        return this;
    }

    public void syncLaunch(Function<Integer, MeterClient> clientFunction) {
        Objects.requireNonNull(uri);
        if (endId < startId)
            throw new RuntimeException("endId must be equal or greater than startId");
        if (heartBeatTime <= 0)
            heartBeatTime = 10000;
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(threadCount);
        List<MeterClient> clients = IntStream.rangeClosed(startId, endId)
                .mapToObj(clientFunction::apply)
                .collect(Collectors.toList());
        clients.forEach(MeterClient::init);
        clients.forEach(client -> {
            try {
                client.syncConnect(uri, eventLoopGroup);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        for (;;) {
            clients.forEach(MeterClient::sendHeartBeat);
            try {
                Thread.sleep(heartBeatTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

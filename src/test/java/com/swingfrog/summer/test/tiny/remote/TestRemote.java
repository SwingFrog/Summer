package com.swingfrog.summer.test.tiny.remote;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.server.exception.CodeException;

@Remote
public class TestRemote {

    public String hello(String name) {
        return "hi," + name + "!";
    }

    public void blank() {
        Summer.getServerPush().syncPushToAll("TestPush", "notice", ImmutableMap.of("msg", "hello"));
    }

    public void error() {
        throw new CodeException(12345, "错误");
    }

}

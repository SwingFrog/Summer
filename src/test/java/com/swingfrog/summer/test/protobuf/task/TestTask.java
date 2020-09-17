package com.swingfrog.summer.test.protobuf.task;

import com.swingfrog.summer.annotation.IntervalTask;
import com.swingfrog.summer.annotation.Task;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.test.protobuf.proto.TestProto;

@Task
public class TestTask {

    @IntervalTask(10000)
    public void sendNotice() {
        Summer.getServerPush().asyncPushToAll(TestProto.Notice_Push_103
                .newBuilder()
                .setValue(System.currentTimeMillis())
                .build());
    }
}

package com.swingfrog.summer.test.protobuf.remote;

import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.async.AsyncResponseMgr;
import com.swingfrog.summer.test.protobuf.proto.TestProto;

@Remote
public class TestRemote {

    public TestProto.HelloWorld_Resp_101 helloWorld(TestProto.HelloWorld_Req_101 req) {
        return TestProto.HelloWorld_Resp_101.newBuilder()
                .setMsg("hello world!")
                .build();
    }

    // void 当做异步返回
    public void add(SessionContext sctx, ProtobufRequest protobufRequest, TestProto.Add_Req_102 req) {
        int a = req.getA();
        int b = req.getB();
        AsyncResponseMgr.get().process(sctx, protobufRequest, () ->
                TestProto.Add_Resp_102.newBuilder()
                .setSum(a + b)
                .build());
    }

}

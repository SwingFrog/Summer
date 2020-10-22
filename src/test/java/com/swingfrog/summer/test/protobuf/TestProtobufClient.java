package com.swingfrog.summer.test.protobuf;

import com.swingfrog.summer.meter.protobuf.MeterWSPSClient;
import com.swingfrog.summer.meter.MeterClientGroup;
import com.swingfrog.summer.promise.Promise;
import com.swingfrog.summer.promise.PromisePool;
import com.swingfrog.summer.test.protobuf.proto.TestProto;
import com.swingfrog.summer.util.ThreadCountUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestProtobufClient extends MeterWSPSClient {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(ThreadCountUtil.cpuDenseness(0));
    private static final PromisePool PROMISE_POOL = new PromisePool();

    public TestProtobufClient(int id) {
        super(id);
    }

    @Override
    public void init() {
        registerPush(new Push<TestProto.Notice_Push_103>() {
            @Override
            protected void handle(TestProto.Notice_Push_103 resp) {
                print("notice value:" + resp.getValue());
            }
        });
    }

    @Override
    protected void online() {
        print("online");
        PROMISE_POOL.createPromise()
                .then(helloWorldTask())
                .then(noticeTask())
                .then(addTask())
                .then(this::close)
                .setExecutor(EXECUTOR)
                .start();
    }

    private Promise.ConsumerTask helloWorldTask() {
        return Promise.newTask(context -> {
            context.waitFuture();
            TestProto.HelloWorld_Req_101 req = TestProto.HelloWorld_Req_101.getDefaultInstance();
            req(req, new Callback<TestProto.HelloWorld_Resp_101>() {
                @Override
                protected void success(TestProto.HelloWorld_Resp_101 resp) {
                    print("helloWorld msg:" + resp.getMsg());
                    context.successFuture();
                }

                @Override
                protected void failure(long code, String msg) {
                    print("helloWorld code:" + code + " msg:" + msg);
                    context.successFuture();
                }
            });
        });
    }

    private Promise.ConsumerTask noticeTask() {
        return Promise.newTask(context -> {
            context.waitFuture();
            oncePush(new Push<TestProto.Notice_Push_103>() {
                @Override
                protected void handle(TestProto.Notice_Push_103 resp) {
                    print("once push success");
                    context.successFuture();
                }
            });
        });
    }

    private Promise.ConsumerTask addTask() {
        return Promise.newTask(context -> {
            context.waitFuture();
            TestProto.Add_Req_102 req = TestProto.Add_Req_102.newBuilder().setA(id).setB(id).build();
            req(req, new Callback<TestProto.Add_Resp_102>() {
                @Override
                protected void success(TestProto.Add_Resp_102 resp) {
                    print("add sum:" + resp.getSum());
                    context.successFuture();
                }

                @Override
                protected void failure(long code, String msg) {
                    print("add code:" + code + " msg:" + msg);
                    context.successFuture();
                }
            });
        });
    }

    @Override
    protected void offline() {
        print("offline");
    }

    @Override
    protected void heartBeat(long serverTime) {
        print("serverTime:" + serverTime);
    }

    private void print(Object content) {
        System.out.println(id + " " + content);
    }

    public static void main(String[] args) throws URISyntaxException {
        MeterClientGroup.create()
                .uri(new URI("ws://127.0.0.1:8828/protobuf"))
                .rangeClosed(1, 1)
                .syncLaunch(TestProtobufClient::new);
    }

}

package com.swingfrog.summer.test.server.slave;

import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.meter.MeterClientGroup;
import com.swingfrog.summer.meter.string.MeterWSSClient;
import com.swingfrog.summer.promise.Promise;
import com.swingfrog.summer.promise.PromisePool;
import com.swingfrog.summer.test.server.slave.bean.ReqQuery;
import com.swingfrog.summer.test.server.slave.bean.RespQuery;
import com.swingfrog.summer.util.ThreadCountUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSlaveClient extends MeterWSSClient {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(ThreadCountUtil.cpuDenseness(0));
    private static final PromisePool PROMISE_POOL = new PromisePool();

    protected TestSlaveClient(int id) {
        super(id);
    }

    @Override
    protected String getPassword() {
        return "123456";
    }

    @Override
    public void init() {
        registerPush("SlavePush", "msg", new Push<String>() {
            @Override
            protected void handle(String resp) {
                print("push msg:" + resp);
            }
        });
    }

    @Override
    protected void online() {
        print("online");
        PROMISE_POOL.createPromise()
                .then(hello())
                .then(add())
                .then(commit())
                .then(query())
                .then(oncePush())
                .then(queryList())
                .then(this::close)
                .setExecutor(EXECUTOR)
                .start();
    }

    private Promise.ConsumerTask hello() {
        return Promise.newTask(context -> {
            context.waitFuture();
            req("SlaveRemote", "hello", null, new Callback<String>() {
                @Override
                protected void success(String resp) {
                    print("hello: " + resp);
                    context.successFuture();
                }

                @Override
                protected void failure(long code, String msg) {
                    print("hello code:" + code + " msg:" + msg);
                    context.successFuture();
                }
            });
        });
    }

    private Promise.ConsumerTask add() {
        return Promise.newTask(context -> {
            context.waitFuture();
            req("SlaveRemote", "add", ImmutableMap.of("a", 1, "b", 2), new Callback<Integer>() {
                @Override
                protected void success(Integer resp) {
                    print("add: " + resp);
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

    private Promise.ConsumerTask commit() {
        return Promise.newTask(context -> {
            context.waitFuture();
            req("SlaveRemote", "commit", ImmutableMap.of("msg", "hello"), new Callback<Void>() {
                @Override
                protected void success(Void resp) {
                    print("commit: " + resp);
                    context.successFuture();
                }

                @Override
                protected void failure(long code, String msg) {
                    print("commit code:" + code + " msg:" + msg);
                    context.successFuture();
                }
            });
        });
    }

    private Promise.ConsumerTask query() {
        return Promise.newTask(context -> {
            context.waitFuture();
            req("SlaveRemote", "query", ImmutableMap.of("id", "123"), new Callback<RespQuery>() {
                @Override
                protected void success(RespQuery resp) {
                    print("query: " + resp);
                    context.successFuture();
                }

                @Override
                protected void failure(long code, String msg) {
                    print("query code:" + code + " msg:" + msg);
                    context.successFuture();
                }
            });
        });
    }

    private Promise.ConsumerTask queryList() {
        return Promise.newTask(context -> {
            context.waitFuture();
            req("SlaveRemote", "queryList", ImmutableMap.of("reqQuery", new ReqQuery(456, 10)), new Callback<List<RespQuery>>() {
                @Override
                protected void success(List<RespQuery> resp) {
                    resp.forEach(respQuery -> print("queryList query id:" + respQuery.getId() + " content:" + respQuery.getContent()));
                    context.successFuture();
                }

                @Override
                protected void failure(long code, String msg) {
                    print("queryList code:" + code + " msg:" + msg);
                    context.successFuture();
                }
            });
        });
    }

    private Promise.ConsumerTask oncePush() {
        return Promise.newTask(context -> {
            context.waitFuture();
            oncePush("SlavePush", "msg", new Push<String>() {
                @Override
                protected void handle(String resp) {
                    print("once push success");
                    context.successFuture();
                }
            });
        });
    }

    @Override
    protected void offline() {
        print("offline");
    }

    private void print(Object content) {
        System.out.println(id + " " + content);
    }

    public static void main(String[] args) throws URISyntaxException {
        MeterClientGroup.create()
                .uri(new URI("ws://127.0.0.1:8829/slave_s1"))
                .rangeClosed(1, 1)
                .syncLaunch(TestSlaveClient::new);
    }

}

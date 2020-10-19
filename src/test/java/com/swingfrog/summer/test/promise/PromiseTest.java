package com.swingfrog.summer.test.promise;

import com.swingfrog.summer.promise.PromiseContext;
import com.swingfrog.summer.promise.PromiseFuture;
import com.swingfrog.summer.promise.PromisePool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PromiseTest {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final PromisePool PROMISE_POOL = new PromisePool();

    public static void main(String[] args) {

        int VALUE_TEST = 0;
        int MARK_FOR_COUNT = 1;
        int MARK_FOR_CONDITION = 2;

        PROMISE_POOL.createPromise()
                .then(task1())
                .then(() -> System.out.println(1))
                .then(() -> System.out.println(2))
                .then(context -> context.put(VALUE_TEST, "hello world!"))
                .then(task2())
                .then(() -> System.out.println(3))
                .then(context -> {
                    String test = context.get(VALUE_TEST);
                    System.out.println(test);
                })
                .mark(MARK_FOR_COUNT)
                .then(() -> System.out.println(4))
                .gotoMark(MARK_FOR_COUNT, 4)
                .then(() -> System.out.println(5))
                .mark(MARK_FOR_CONDITION)
                .then(() -> System.out.println(6))
                .gotoMark(MARK_FOR_CONDITION, context -> context.remove(VALUE_TEST) != null)
                .then(EXECUTOR::shutdown)
                .setCatch(Throwable::printStackTrace)
                .setExecutor(EXECUTOR)
                .start();
    }

    private static Consumer<PromiseContext> task1() {
        return context -> {
            context.waitFuture();
            PROMISE_POOL.createPromise()
                    .then(() -> System.out.println("A"))
                    .then(child -> {
                        //int a = 1 / 0;
                        child.waitFuture();
                        new Thread(() -> {
                            System.out.println("waitA...");
                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("overA...");
                            child.successFuture();
                        }).start();
                    })
                    .then(() -> System.out.println("B"))
                    .then(context::successFuture)
                    .setCatch(context::failureFuture)
                    .setExecutor(EXECUTOR)
                    .start();
        };
    }

    private static Consumer<PromiseContext> task2() {
        return context -> {
            PromiseFuture future1 = context.waitFuture();
            new Thread(() -> {
                System.out.println("wait1...");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("over1...");
                future1.success();
            }).start();

            PromiseFuture future2 = context.waitFuture();
            new Thread(() -> {
                System.out.println("wait2...");
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("over2...");
                future2.success();
            }).start();
        };
    }

}

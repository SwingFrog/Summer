package com.swingfrog.summer.test.promise;

import com.swingfrog.summer.promise.Promise;
import com.swingfrog.summer.promise.PromiseFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PromiseTest {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Promise.create()
                .then(() -> System.out.println(1))
                .then(() -> System.out.println(2))
                .then(context -> context.put("TEST", "hello world!"))
                .then(context -> {
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
                })
                .then(() -> System.out.println(3))
                .then(context -> {
                    String test = context.get("TEST");
                    System.out.println(test);
                })
                .then(executor::shutdown)
                .setCatch(Throwable::printStackTrace)
                .setExecutor(executor)
                .start();
    }

}

package com.swingfrog.summer.promise;

import com.google.common.collect.Queues;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class Promise {

    private volatile boolean running;
    private Executor executor;
    private final Queue<Runnable> queue = Queues.newConcurrentLinkedQueue();
    private final PromiseContext context = new PromiseContext(this);
    private Consumer<Throwable> throwableConsumer;

    private Promise() {

    }

    public static Promise create() {
        return new Promise();
    }

    public Promise then(Consumer<PromiseContext> consumer) {
        queue.add(() -> {
            try {
                consumer.accept(context);
                next();
            } catch (Throwable throwable) {
                throwError(throwable);
            }
        });
        return this;
    }

    public Promise then(Runnable runnable) {
        queue.add(() -> {
            try {
                runnable.run();
                next();
            } catch (Throwable throwable) {
                throwError(throwable);
            }
        });
        return this;
    }

    void throwError(Throwable throwable) {
        queue.clear();
        context.clear();
        running = false;
        if (throwableConsumer != null)
            throwableConsumer.accept(throwable);
    }

    public Promise setCatch(Consumer<Throwable> consumer) {
        this.throwableConsumer = consumer;
        return this;
    }

    void next() {
        if (queue.isEmpty()) {
            context.clear();
            running = false;
            return;
        }
        if (context.hasWaitFuture())
            return;
        Runnable runnable = queue.poll();
        if (runnable == null) {
            context.clear();
            running = false;
            return;
        }
        if (executor != null) {
            executor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    public void start(Executor executor) {
        if (running)
            throw new UnsupportedOperationException();
        running = true;
        this.executor = executor;
        queue.clear();
        context.clear();
        next();
    }

    public void start() {
        start(null);
    }

    public boolean isStop() {
        return queue.isEmpty();
    }

}

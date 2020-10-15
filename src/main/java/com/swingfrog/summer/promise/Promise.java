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

    void throwError(Throwable throwable) {
        stop();
        if (throwableConsumer != null)
            throwableConsumer.accept(throwable);
    }

    void next() {
        if (queue.isEmpty()) {
            stop();
            return;
        }
        if (context.hasWaitFuture())
            return;
        Runnable runnable = queue.poll();
        if (runnable == null) {
            stop();
            return;
        }
        if (executor != null) {
            executor.execute(runnable);
        } else {
            runnable.run();
        }
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

    public Promise setCatch(Consumer<Throwable> consumer) {
        this.throwableConsumer = consumer;
        return this;
    }

    public Promise setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public void start() {
        if (running)
            throw new UnsupportedOperationException();
        running = true;
        context.clear();
        next();
    }

    public void stop() {
        queue.clear();
        context.clear();
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

}

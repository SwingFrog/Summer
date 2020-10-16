package com.swingfrog.summer.promise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Promise {

    private volatile boolean running;
    private Executor executor;
    private Consumer<Throwable> throwableConsumer;
    private final AtomicInteger executeIndex = new AtomicInteger();
    private final List<Runnable> runnableList = new ArrayList<>();
    // name, index
    private final Map<String, Integer> markIndexMap = new HashMap<>();
    private final PromiseContext context = new PromiseContext(this);
    // index, gotoCount
    private final Map<Integer, Integer> gotoCountMap = new HashMap<>();

    private Promise() {

    }

    void throwError(Throwable throwable) {
        stop();
        if (throwableConsumer != null)
            throwableConsumer.accept(throwable);
    }

    void next() {
        int nextIndex = executeIndex.get();
        if (nextIndex >= runnableList.size()) {
            stop();
            return;
        }
        if (context.hasWaitFuture())
            return;
        nextIndex = executeIndex.getAndIncrement();
        if (nextIndex >= runnableList.size()) {
            stop();
            return;
        }
        Runnable runnable = runnableList.get(nextIndex);
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
        runnableList.add(() -> {
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
        runnableList.add(() -> {
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

    public Promise mark(String name) {
        markIndexMap.put(name, runnableList.size());
        return this;
    }

    public Promise gotoMark(String name) {
        runnableList.add(() -> {
            Integer index = markIndexMap.get(name);
            if (index != null) {
                executeIndex.set(index);
            }
            next();
        });
        return this;
    }

    public Promise gotoMark(String name, int count) {
        int currentIndex = runnableList.size();
        runnableList.add(() -> {
            Integer index = markIndexMap.get(name);
            if (index != null) {
                int currentCount = gotoCountMap.getOrDefault(currentIndex, 0);
                if (currentCount < count) {
                    executeIndex.set(index);
                    gotoCountMap.put(currentIndex, currentCount + 1);
                }
            }
            next();
        });
        return this;
    }

    public Promise gotoMark(String name, Predicate<PromiseContext> predicate) {
        runnableList.add(() -> {
            Integer index = markIndexMap.get(name);
            if (index != null && predicate.test(context)) {
                executeIndex.set(index);
            }
            next();
        });
        return this;
    }

    public void start() {
        if (running)
            throw new UnsupportedOperationException();
        stop();
        running = true;
        next();
    }

    public void stop() {
        executeIndex.set(0);
        context.clear();
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

}

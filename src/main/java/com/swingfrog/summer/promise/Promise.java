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
    private Runnable stopHook;
    private final AtomicInteger executeIndex = new AtomicInteger();
    private final List<Runnable> runnableList = new ArrayList<>();
    // key, index
    private final Map<Object, Integer> markIndexMap = new HashMap<>();
    private final PromiseContext context = new PromiseContext(this);
    // index, gotoCount
    private final Map<Integer, Integer> gotoCountMap = new HashMap<>();
    // index, executor
    private final Map<Integer, Executor> executorMap = new HashMap<>();

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
        Executor availableExecutor = executorMap.getOrDefault(nextIndex, executor);
        if (availableExecutor != null) {
            availableExecutor.execute(runnable);
            return;
        }
        runnable.run();
    }

    void setStopHook(Runnable runnable) {
        checkNotRunning();
        stopHook = runnable;
    }

    public static Promise create() {
        return new Promise();
    }

    public Promise then(Consumer<PromiseContext> consumer) {
        checkNotRunning();
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
        checkNotRunning();
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

    public Promise then(Consumer<PromiseContext> consumer, Executor executor) {
        int index = runnableList.size();
        then(consumer);
        executorMap.put(index, executor);
        return this;
    }

    public Promise then(Runnable runnable, Executor executor) {
        int index = runnableList.size();
        then(runnable);
        executorMap.put(index, executor);
        return this;
    }

    public Promise then(ConsumerTask contextConsumerTask) {
        return then(contextConsumerTask.consumer, contextConsumerTask.executor);
    }

    public Promise then(RunnableTask runnableTask) {
        return then(runnableTask.runnable, runnableTask.executor);
    }

    public Promise setCatch(Consumer<Throwable> consumer) {
        checkNotRunning();
        throwableConsumer = consumer;
        return this;
    }

    public Promise setExecutor(Executor executor) {
        checkNotRunning();
        this.executor = executor;
        return this;
    }

    public Promise mark(Object key) {
        checkNotRunning();
        markIndexMap.put(key, runnableList.size());
        return this;
    }

    public Promise gotoMark(Object key) {
        checkNotRunning();
        runnableList.add(() -> {
            Integer index = markIndexMap.get(key);
            if (index != null) {
                executeIndex.set(index);
            }
            next();
        });
        return this;
    }

    public Promise gotoMark(Object key, int count) {
        checkNotRunning();
        int currentIndex = runnableList.size();
        runnableList.add(() -> {
            Integer index = markIndexMap.get(key);
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

    public Promise gotoMark(Object key, Predicate<PromiseContext> predicate) {
        checkNotRunning();
        runnableList.add(() -> {
            Integer index = markIndexMap.get(key);
            if (index != null && predicate.test(context)) {
                executeIndex.set(index);
            }
            next();
        });
        return this;
    }

    public void start() {
        checkNotRunning();
        executeIndex.set(0);
        context.clear();
        running = true;
        next();
    }

    public void stop() {
        executeIndex.set(0);
        context.clear();
        running = false;
        if (stopHook != null)
            stopHook.run();
    }

    public boolean isRunning() {
        return running;
    }

    public void clearRunnable() {
        runnableList.clear();
    }

    private void checkNotRunning() {
        if (running)
            throw new UnsupportedOperationException();
    }

    public static class ConsumerTask {
        private final Consumer<PromiseContext> consumer;
        private final Executor executor;
        public ConsumerTask(Consumer<PromiseContext> consumer, Executor executor) {
            this.consumer = consumer;
            this.executor = executor;
        }
    }

    public static class RunnableTask {
        private final Runnable runnable;
        private final Executor executor;
        public RunnableTask(Runnable runnable, Executor executor) {
            this.runnable = runnable;
            this.executor = executor;
        }
    }

    public static ConsumerTask newTask(Consumer<PromiseContext> consumer, Executor executor) {
        return new ConsumerTask(consumer, executor);
    }

    public static RunnableTask newTask(Runnable runnable, Executor executor) {
        return new RunnableTask(runnable, executor);
    }

}

package com.swingfrog.summer.db.repository;

import com.google.common.collect.Sets;
import com.swingfrog.summer.config.ConfigUtil;
import com.swingfrog.summer.util.ThreadCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncCacheRepositoryMgr {

    private static final Logger log = LoggerFactory.getLogger(AsyncCacheRepositoryMgr.class);

    private static ScheduledExecutorService scheduledExecutor;
    private final AsyncCacheConfig config = new AsyncCacheConfig();
    private final Set<Runnable> hooks = Sets.newConcurrentHashSet();

    private static class SingleCase {
        public static final AsyncCacheRepositoryMgr INSTANCE = new AsyncCacheRepositoryMgr();
    }

    private AsyncCacheRepositoryMgr() {

    }

    public static AsyncCacheRepositoryMgr get() {
        return AsyncCacheRepositoryMgr.SingleCase.INSTANCE;
    }

    public void loadConfig(String path) throws IOException, IntrospectionException {
        Properties pro = new Properties();
        FileInputStream in = new FileInputStream(path);
        pro.load(in);
        ConfigUtil.loadDataWithBean(pro, "asyncCache.", config);
        in.close();
        pro.clear();
        config.setCoreThread(ThreadCountUtil.convert(config.getCoreThread()));
        log.info("async cache repository manager loading config, core thread num[{}]", config.getCoreThread());
    }

    public void shutdown() {
        if (scheduledExecutor != null) {
            log.info("async cache repository manager shutdown");
            scheduledExecutor.shutdown();
            try {
                while (!scheduledExecutor.isTerminated()) {
                    scheduledExecutor.awaitTermination(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e){
                log.error(e.getMessage(), e);
            }
        }
        triggerHook();
    }

    public ScheduledExecutorService getScheduledExecutor() {
        if (scheduledExecutor == null) {
            synchronized (this) {
                if (scheduledExecutor == null) {
                    scheduledExecutor = Executors.newScheduledThreadPool(
                            config.getCoreThread(),
                            new DefaultThreadFactory("AsyncCacheRepositoryMgr"));
                    log.info("async cache repository manager create scheduled executor");
                }
            }
        }
        return scheduledExecutor;
    }

    public void addHook(Runnable hook) {
        hooks.add(hook);
    }

    public void triggerHook() {
        hooks.forEach(Runnable::run);
    }

}

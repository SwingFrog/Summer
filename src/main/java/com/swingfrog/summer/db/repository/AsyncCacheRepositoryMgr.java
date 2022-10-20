package com.swingfrog.summer.db.repository;

import com.google.common.collect.Lists;
import com.swingfrog.summer.config.ConfigUtil;
import com.swingfrog.summer.db.DataBaseMgr;
import com.swingfrog.summer.util.ThreadCountUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncCacheRepositoryMgr {

    private static final Logger log = LoggerFactory.getLogger(AsyncCacheRepositoryMgr.class);

    private volatile ScheduledExecutorService scheduledExecutor;
    private final AsyncCacheConfig config = new AsyncCacheConfig();
    private final List<Runnable> hooks = Lists.newArrayList();

    private static class SingleCase {
        public static final AsyncCacheRepositoryMgr INSTANCE = new AsyncCacheRepositoryMgr();
    }

    private AsyncCacheRepositoryMgr() {

    }

    public static AsyncCacheRepositoryMgr get() {
        return AsyncCacheRepositoryMgr.SingleCase.INSTANCE;
    }

    public void loadConfig(String path) throws IOException, IntrospectionException {
        if (DataBaseMgr.DEFAULT_CONFIG_PATH.equals(path)) {
            File file = new File(path);
            if (file.exists()) {
                loadConfig(new FileInputStream(file));
            } else {
                loadDefaultConfig();
            }
        } else {
            loadConfig(new FileInputStream(path));
        }
        log.info("async cache repository manager loading config, core thread num[{}]", config.getCoreThread());
    }

    public void loadConfig(InputStream in) throws IOException, IntrospectionException {
        Properties pro = new Properties();
        pro.load(in);
        ConfigUtil.loadDataWithBean(pro, "asyncCache.", config);
        in.close();
        pro.clear();
        config.setCoreThread(ThreadCountUtil.ioDenseness(config.getCoreThread()));
    }

    private void loadDefaultConfig() {
        config.setCoreThread(1);
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

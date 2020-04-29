package com.swingfrog.summer.db.repository;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.swingfrog.summer.db.DaoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class AsyncCacheRepositoryDao<T, K> extends CacheRepositoryDao<T, K> {

    private static final Logger log = LoggerFactory.getLogger(AsyncCacheRepositoryDao.class);

    private final ConcurrentLinkedQueue<Change<T, K>> waitChange = Queues.newConcurrentLinkedQueue();
    private final ConcurrentMap<T, Long> waitSave = Maps.newConcurrentMap();
    private long delayTime = delayTime();
    private final Set<K> waitAdd = Sets.newConcurrentHashSet();

    protected abstract long delayTime();

    @Override
    void init() {
        super.init();
        AsyncCacheRepositoryMgr.get().getScheduledExecutor().scheduleWithFixedDelay(
                () -> delay(false),
                delayTime,
                delayTime,
                TimeUnit.MILLISECONDS);
        AsyncCacheRepositoryMgr.get().addHook(() -> delay(true));
        if (delayTime >= expireTime()) {
            throw new DaoRuntimeException(String.format("async cache repository delayTime[%s] must be less than expireTime[%s]", delayTime, expireTime()));
        }
    }

    private synchronized void delay(boolean force) {
        try {
            while (!waitChange.isEmpty()) {
                Change<T, K> change = waitChange.poll();
                if (change.add) {
                    delayAdd(change.obj, change.pk);
                } else {
                    delayRemove(change.pk);
                }
            }
            delaySave(force);
        } catch (Throwable e) {
            log.error("AsyncCacheRepository delay failure.");
            log.error(e.getMessage(), e);
        }
    }

    private void delayAdd(T obj, K primaryKey) {
        if (waitAdd.remove(primaryKey)) {
            super.addByPrimaryKeyNotAddCache(obj, primaryKey);
        }
    }

    private void delayRemove(K pk) {
        super.removeByPrimaryKeyNotRemoveCache(pk);
    }

    private void delaySave(boolean force) {
        long time = System.currentTimeMillis();
        List<T> list = waitSave.entrySet().stream()
                .filter(entry -> force || time - entry.getValue() >= delayTime)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (!list.isEmpty()) {
            super.save(list);
        }
        list.stream().filter(k -> force || time - waitSave.get(k) >= delayTime).forEach(waitSave::remove);
        log.info("async cache repository table[{}] delay save nowSaveCount[{}] waitSaveCount[{}]", tableMeta.getName(), list.size(), waitSave.size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean add(T obj) {
        super.autoIncrementPrimaryKey(obj);
        K primaryKey = (K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj);
        super.addCache(primaryKey, obj);
        waitAdd.add(primaryKey);
        waitChange.add(new Change<>(obj, primaryKey));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(T obj) {
        return removeByPrimaryKey((K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj));
    }

    @Override
    public boolean removeByPrimaryKey(K primaryKey) {
        super.removeCacheByPrimaryKey(primaryKey);
        if (waitAdd.remove(primaryKey)) {
            return true;
        }
        waitChange.add(new Change<>(primaryKey));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean save(T obj) {
        Objects.requireNonNull(obj, "async cache repository save param not null");
        K pk = (K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj);
        T newObj = get(pk);
        if (obj != newObj) {
            log.warn("async cache repository table[{}] primary key[{}] expire, can't save", tableMeta.getName(), pk);
            return false;
        }
        waitSave.computeIfAbsent(obj, k -> System.currentTimeMillis());
        return true;
    }

    @Override
    public void save(List<T> objs) {
        objs.forEach(this::save);
    }

    private static class Change<T, K> {
        T obj;
        K pk;
        boolean add;
        Change(T obj, K pk) {
            this.obj = obj;
            this.pk = pk;
            this.add = true;
        }
        Change(K pk) {
            this.pk = pk;
            this.add = false;
        }
    }

    public boolean syncAdd(T obj) {
        return super.add(obj);
    }

    public boolean syncRemove(T obj) {
        return super.remove(obj);
    }

    public boolean syncRemoveByPrimaryKey(K primaryKey) {
        return super.removeByPrimaryKey(primaryKey);
    }

    public boolean syncSave(T obj) {
        return super.save(obj);
    }

    public void syncSave(List<T> objs) {
        super.save(objs);
    }

}

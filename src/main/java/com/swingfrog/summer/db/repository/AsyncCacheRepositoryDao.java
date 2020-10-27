package com.swingfrog.summer.db.repository;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.swingfrog.summer.db.DaoRuntimeException;
import com.swingfrog.summer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AsyncCacheRepositoryDao<T, K> extends CacheRepositoryDao<T, K> {

    private static final Logger log = LoggerFactory.getLogger(AsyncCacheRepositoryDao.class);
    private static final String PREFIX = "AsyncCacheRepositoryDao";
    private final ConcurrentLinkedQueue<Change<T, K>> waitChange = Queues.newConcurrentLinkedQueue();
    private final ConcurrentMap<K, Save<T, K>> waitSave = Maps.newConcurrentMap();
    private final Set<K> waitAdd = Sets.newConcurrentHashSet();
    long delayTime;

    protected abstract long delayTime();

    @Override
    void init() {
        super.init();
        delayTime = delayTime();
        AsyncCacheRepositoryMgr.get().getScheduledExecutor().scheduleWithFixedDelay(
                () -> delay(false),
                delayTime,
                delayTime,
                TimeUnit.MILLISECONDS);
        AsyncCacheRepositoryMgr.get().addHook(() -> delay(true));
        if (!isNeverExpire() && delayTime >= expireTime) {
            throw new DaoRuntimeException(String.format("async cache repository delayTime[%s] must be less than expireTime[%s]", delayTime, expireTime));
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

        List<Save<T, K>> saves = waitSave.values().stream()
                .filter(value -> force || time - value.saveTime >= delayTime)
                .collect(Collectors.toList());

        if (!saves.isEmpty()) {
            saves.stream().map(value -> value.obj).forEach(super::save);
        }

        saves.stream().filter(value -> force || time - value.saveTime >= delayTime).forEach(value -> waitSave.remove(value.pk));
        log.info("async cache repository table[{}] delay save nowSaveCount[{}] waitSaveCount[{}]", getTableMeta().getName(), saves.size(), waitSave.size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean add(T obj) {
        super.autoIncrementPrimaryKey(obj);
        K primaryKey = (K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj);
        super.addCache(primaryKey, obj);
        waitAdd.add(primaryKey);
        waitChange.add(new Change<>(obj, primaryKey));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(T obj) {
        return removeByPrimaryKey((K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj));
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
        K pk = (K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj);
        T newObj = get(pk);
        if (obj != newObj) {
            log.warn("async cache repository table[{}] primary key[{}] expire, can't save", getTableMeta().getName(), pk);
            return false;
        }
        waitSave.computeIfAbsent(pk, k -> new Save<>(obj, pk, System.currentTimeMillis()));
        return true;
    }

    @Override
    public void save(Collection<T> objs) {
        objs.forEach(this::save);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forceSave(T obj) {
        Objects.requireNonNull(obj, "async cache repository force save param not null");
        K pk = (K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj);
        forceAddCache(pk, obj);
        waitSave.computeIfAbsent(pk, k -> new Save<>(obj, pk, System.currentTimeMillis()));
    }

    @Override
    public T getOrCreate(K primaryKey, Supplier<T> supplier) {
        T entity = get(primaryKey);
        if (entity == null) {
            synchronized (StringUtil.getString(PREFIX, getTableMeta().getName(), "getOrCreate", primaryKey)) {
                entity = get(primaryKey);
                if (entity == null) {
                    entity = supplier.get();
                    add(entity);
                }
            }
        }
        return entity;
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

    private static class Save<T, K> {
        T obj;
        K pk;
        long saveTime;
        Save(T obj, K pk, long saveTime) {
            this.obj = obj;
            this.pk = pk;
            this.saveTime = saveTime;
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

    public void syncSave(Collection<T> objs) {
        super.save(objs);
    }

    public T syncGetOrCreate(K primaryKey, Supplier<T> supplier) {
        return super.getOrCreate(primaryKey, supplier);
    }
}

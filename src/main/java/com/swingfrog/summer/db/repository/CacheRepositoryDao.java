package com.swingfrog.summer.db.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer.db.DaoRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CacheRepositoryDao<T, K> extends RepositoryDao<T, K> {

    private static final Logger log = LoggerFactory.getLogger(CacheRepositoryDao.class);

    private T EMPTY;
    private Cache<K, T> cache;
    private final Map<String, Cache<Object, Set<K>>> cachePkMap = Maps.newHashMap();
    private final Map<String, Cache<Object, Boolean>> cachePkFinishMap = Maps.newHashMap();
    private final AtomicLong findAllTime = new AtomicLong(0);
    long expireTime;
    volatile boolean waitRemoveAll;

    // never expire if value less then zero
    protected abstract long expireTime();

    @Override
    void init() {
        super.init();
        expireTime = expireTime();
        try {
            EMPTY = getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DaoRuntimeException("cache repository EMPTY not null");
        }
        if (isNeverExpire()) {
            cache = CacheBuilder.newBuilder().build();
            getTableMeta().getCacheKeys().forEach(columnMeta -> {
                cachePkMap.put(columnMeta.getName(), CacheBuilder.newBuilder().build());
                cachePkFinishMap.put(columnMeta.getName(), CacheBuilder.newBuilder().build());
            });
        } else {
            cache = CacheBuilder.newBuilder()
                    .expireAfterAccess(expireTime, TimeUnit.MILLISECONDS)
                    .build();
            getTableMeta().getCacheKeys().forEach(columnMeta -> {
                cachePkMap.put(columnMeta.getName(),
                        CacheBuilder.newBuilder()
                                .expireAfterAccess(expireTime, TimeUnit.MILLISECONDS)
                                .build());
                cachePkFinishMap.put(columnMeta.getName(),
                        CacheBuilder.newBuilder()
                                .expireAfterAccess(expireTime, TimeUnit.MILLISECONDS)
                                .build());
            });
        }

    }

    boolean isNeverExpire() {
        return expireTime < 0;
    }

    @Override
    protected T addNotAutoIncrement(T obj) {
        T old = addCacheIfAbsent(obj);
        if (old != null && old != EMPTY)
            return old;
        T result = super.addNotAutoIncrement(obj);
        if (result == null) {
            log.error("CacheRepository addNotAutoIncrement failure.");
            log.error(obj.toString());
        }
        return result;
    }

    @Override
    protected T addByPrimaryKey(T obj, K primaryKey) {
        T old = addCacheIfAbsent(obj);
        if (old != null && old != EMPTY)
            return old;
        T result = super.addByPrimaryKey(obj, primaryKey);
        if (result == null) {
            log.error("CacheRepository addByPrimaryKey failure.");
            log.error("{} {}", obj.toString(), primaryKey.toString());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T add(T obj) {
        Objects.requireNonNull(obj);
        super.autoIncrementPrimaryKey(obj);
        T old = addCacheIfAbsent(obj);
        if (old != null && old != EMPTY)
            return old;
        T result = super.addByPrimaryKey(obj, (K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj));
        if (result == null) {
            log.error("CacheRepository add failure.");
            log.error(obj.toString());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(T obj) {
        Objects.requireNonNull(obj);
        return removeByPrimaryKey((K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj));
    }

    @Override
    public boolean removeByPrimaryKey(K primaryKey) {
        boolean ok = super.removeByPrimaryKey(primaryKey);
        if (ok) {
            removeCacheByPrimaryKey(primaryKey);
        } else {
            log.error("CacheRepository removeByPrimaryKey failure.");
            log.error(primaryKey.toString());
        }
        return ok;
    }

    @Override
    public void removeAll() {
        waitRemoveAll = true;
        super.removeAll();
        removeAllCache();
        waitRemoveAll = false;
    }

    @Override
    public boolean save(T obj) {
        Objects.requireNonNull(obj);
        return super.save(obj);
    }

    @Override
    public void save(Collection<T> objs) {
        Objects.requireNonNull(objs);
        super.save(objs);
    }

    @Override
    public void forceSave(T obj) {
        Objects.requireNonNull(obj);
        forceAddCache(obj);
        super.save(obj);
    }

    @Override
    public T get(K primaryKey) {
        Objects.requireNonNull(primaryKey);
        T obj = cache.getIfPresent(primaryKey);
        if (obj == null) {
            if (waitRemoveAll) {
                return null;
            }
            obj = super.get(primaryKey);
            obj = obj != null ? obj : EMPTY;
            T old = addCacheIfAbsent(primaryKey, obj);
            if (old != null)
                obj = old;
        }
        if (obj == EMPTY) {
            return null;
        }
        return obj;
    }

    @Override
    public T getOrCreate(K primaryKey, Supplier<T> supplier) {
        Objects.requireNonNull(primaryKey);
        Objects.requireNonNull(supplier);
        T entity = get(primaryKey);
        if (entity == null) {
            entity = supplier.get();
            entity = add(entity);
        }
        return entity;
    }

    @Override
    public List<T> list(String field, Object value) {
        return list(field, value, null);
    }

    @Override
    public List<T> list(String field, Object value, Predicate<T> filter) {
        return list(ImmutableMap.of(field, value), filter);
    }

    @Override
    public List<T> list(Map<String, Object> optional) {
        return list(optional, null);
    }

    @Override
    public List<T> list(Map<String, Object> optional, Predicate<T> filter) {
        Stream<T> stream = stream(optional);
        if (filter != null)
            stream = stream.filter(filter);
        return stream.collect(Collectors.toList());
    }

    @Override
    public List<T> listAll() {
        return listAll(null);
    }

    @Override
    public List<T> listAll(Predicate<T> filter) {
        Stream<T> stream = streamAll();
        if (filter != null)
            stream = stream.filter(filter);
        return stream.collect(Collectors.toList());
    }

    @Override
    public List<T> listSingleCache(Object value) {
        return list(getSingeCacheField(), value);
    }

    @Override
    public List<T> listSingleCache(Object value, Predicate<T> filter) {
        return list(getSingeCacheField(), value, filter);
    }

    protected Set<K> listPrimaryValueByCacheKey(String column, Object cacheValue) {
        Objects.requireNonNull(column);
        Objects.requireNonNull(cacheValue);
        Cache<Object, Set<K>> cachePk = cachePkMap.get(column);
        Set<K> pkSet = cachePk.getIfPresent(cacheValue);
        if (cachePkFinishMap.get(column).getIfPresent(cacheValue) == null || pkSet == null) {
            pkSet = Sets.newConcurrentHashSet();
            Set<K> oldPkSet = cachePk.asMap().putIfAbsent(cacheValue, pkSet);
            if (oldPkSet != null)
                pkSet = oldPkSet;
            cachePkFinishMap.get(column).put(cacheValue, true);
            pkSet.addAll(listPrimaryKey(ImmutableMap.of(column, cacheValue)));
        }
        return pkSet;
    }

    @SuppressWarnings("unchecked")
    protected T addCacheIfAbsent(T obj) {
        return addCacheIfAbsent((K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj), obj);
    }

    // add cache if absent or empty
    protected T addCacheIfAbsent(K primaryKey, T obj) {
        for (;;) {
            T old = cache.getIfPresent(primaryKey);
            if (old != null && old != EMPTY)
                return old;
            old = cache.asMap().putIfAbsent(primaryKey, obj);
            if (old == null) {
                addCache(primaryKey, obj);
                return null;
            } else if (old == EMPTY) {
                old = cache.asMap().computeIfPresent(primaryKey, (k, v) -> v == EMPTY ? obj : v);
                if (old == null) // Theoretically it should not be null.
                    continue;
                if (old == EMPTY)
                    return null;
                if (old == obj) {
                    addCache(primaryKey, obj);
                    return null;
                }
            }
            return old;
        }
    }

    @SuppressWarnings("unchecked")
    protected void forceAddCache(T obj) {
        forceAddCache((K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj), obj);
    }

    protected void forceAddCache(K primaryKey, T obj) {
        cache.put(primaryKey, obj);
        addCache(primaryKey, obj);
    }

    private void addCache(K primaryKey, T obj) {
        if (obj == EMPTY)
            return;
        getTableMeta().getCacheKeys().forEach(columnMeta -> {
            Object cacheValue = TableValueBuilder.getColumnValue(columnMeta, obj);
            Cache<Object, Set<K>> cachePk = cachePkMap.get(columnMeta.getName());
            Set<K> pkSet = cachePk.getIfPresent(cacheValue);
            cachePkFinishMap.get(columnMeta.getName()).getIfPresent(cacheValue);
            if (pkSet == null) {
                pkSet = Sets.newConcurrentHashSet();
                Set<K> oldPkSet = cachePk.asMap().putIfAbsent(cacheValue, pkSet);
                if (oldPkSet != null)
                    pkSet = oldPkSet;
            }
            pkSet.add(primaryKey);
        });
    }

    @SuppressWarnings("unchecked")
    protected void removeCache(T obj) {
        removeCacheByPrimaryKey((K) TableValueBuilder.getPrimaryKeyValue(getTableMeta(), obj));
    }

    protected void removeCacheByPrimaryKey(K primaryKey) {
        cache.put(primaryKey, EMPTY);
        getTableMeta().getCacheKeys().forEach(columnMeta ->
                cachePkMap.get(columnMeta.getName()).asMap().values().stream()
                        .filter(Objects::nonNull)
                        .forEach(pkSet -> pkSet.remove(primaryKey)));
    }

    protected void removeAllCache() {
        cache.asMap().keySet().forEach(k -> cache.put(k, EMPTY));
        cachePkMap.values().forEach(Cache::invalidateAll);
    }

    protected T addByPrimaryKeyNotAddCache(T obj, K primaryKey) {
        T result = super.addByPrimaryKey(obj, primaryKey);
        if (result == null) {
            log.error("CacheRepository addByPrimaryKeyNotAddCache failure.");
            log.error("{} {}", obj.toString(), primaryKey.toString());
        }
        return result;
    }

    protected boolean removeByPrimaryKeyNotRemoveCache(K primaryKey) {
        boolean ok = super.removeByPrimaryKey(primaryKey);
        if (!ok) {
            log.error("CacheRepository removeByPrimaryKeyNotRemoveCache failure.");
            log.error(primaryKey.toString());
        }
        return ok;
    }

    protected void removeAllNotRemoveCache() {
        super.removeAll();
    }

    @Override
    public Stream<T> stream(String field, Object value) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(value);
        if (getTableMeta().getCacheKeys().contains(getTableMeta().getColumnMetaMap().get(field))) {
            return listPrimaryValueByCacheKey(field, value).stream().map(this::get).filter(Objects::nonNull);
        }
        return stream(ImmutableMap.of(field, value));
    }

    @Override
    public Stream<T> stream(Map<String, Object> optional) {
        Objects.requireNonNull(optional);
        List<Set<K>> pkList = null;
        Map<String, Object> normal = null;
        for (Map.Entry<String, Object> entry : optional.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (getTableMeta().getCacheKeys().contains(getTableMeta().getColumnMetaMap().get(key))) {
                if (pkList == null)
                    pkList = Lists.newArrayList();
                pkList.add(listPrimaryValueByCacheKey(key, value));
            } else {
                if (normal == null)
                    normal = Maps.newHashMap();
                normal.put(key, value);
            }
        }
        Stream<T> stream;
        if (pkList == null || pkList.isEmpty()) {
            super.listPrimaryKey(optional).forEach(this::get);
            stream = cache.asMap().values().stream().filter(obj -> obj != EMPTY);
        } else {
            if (pkList.size() == 1) {
                stream = pkList.get(0).stream().map(this::get).filter(Objects::nonNull);
            } else {
                Set<K> first = Sets.newHashSet(pkList.get(0));
                pkList.set(0, null);
                List<Set<K>> finalPkList = pkList;
                first.removeIf(obj -> finalPkList.stream().filter(Objects::nonNull).anyMatch(pk -> !pk.contains(obj)));
                stream = first.stream().map(this::get).filter(Objects::nonNull);
            }
        }
        if (normal != null && !normal.isEmpty()) {
            Map<String, Object> finalNormal = normal;
            stream = stream.filter(obj ->
                    finalNormal.entrySet().stream()
                            .allMatch(entry ->
                                    TableValueBuilder.isEqualsColumnValue(
                                            getTableMeta().getColumnMetaMap().get(entry.getKey()), obj, entry.getValue())));
        }
        return stream;
    }

    @Override
    public Stream<T> streamSingleCache(Object value) {
        Objects.requireNonNull(value);
        return stream(getSingeCacheField(), value);
    }

    @Override
    public Stream<T> streamAll() {
        long time = System.currentTimeMillis();
        if (time - expireTime >= findAllTime.getAndSet(time)) {
            listPrimaryKey().forEach(this::get);
        }
        return cache.asMap().keySet().stream()
                .map(this::get)
                .filter(Objects::nonNull);
    }

}

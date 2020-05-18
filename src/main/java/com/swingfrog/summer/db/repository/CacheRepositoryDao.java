package com.swingfrog.summer.db.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer.db.DaoRuntimeException;
import com.swingfrog.summer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class CacheRepositoryDao<T, K> extends RepositoryDao<T, K> {

    private static final Logger log = LoggerFactory.getLogger(CacheRepositoryDao.class);

    private static final String PREFIX = "CacheRepositoryDao";
    private T EMPTY;
    private final Cache<K, T> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(expireTime(), TimeUnit.MILLISECONDS)
            .build();
    private final Map<String, Cache<Object, Set<K>>> cachePkMap = Maps.newHashMap();
    private final Map<String, Cache<Object, Boolean>> cachePkFinishMap = Maps.newHashMap();
    private final AtomicLong findAllTime = new AtomicLong(0);
    private final long expireTime = expireTime();

    protected abstract long expireTime();

    @Override
    void init() {
        super.init();
        try {
            EMPTY = getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DaoRuntimeException("cache repository EMPTY not null");
        }
        tableMeta.getCacheKeys().forEach(columnMeta -> {
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

    @Override
    protected boolean addNotAutoIncrement(T obj) {
        boolean ok = super.addNotAutoIncrement(obj);
        if (ok) {
            addCache(obj);
        } else {
            log.error("CacheRepository addNotAutoIncrement failure.");
            log.error(obj.toString());
        }
        return ok;
    }

    @Override
    protected boolean addByPrimaryKey(T obj, K primaryKey) {
        boolean ok = super.addByPrimaryKey(obj, primaryKey);
        if (ok) {
            addCache(primaryKey, obj);
        } else {
            log.error("CacheRepository addByPrimaryKey failure.");
            log.error("{} {}", obj.toString(), primaryKey.toString());
        }
        return ok;
    }

    @Override
    public boolean add(T obj) {
        boolean ok = super.add(obj);
        if (ok) {
            addCache(obj);
        } else {
            log.error("CacheRepository add failure.");
            log.error(obj.toString());
        }
        return ok;
    }

    @Override
    public boolean remove(T obj) {
        boolean ok = super.remove(obj);
        if (ok) {
            removeCache(obj);
        } else {
            log.error("CacheRepository remove failure.");
            log.error(obj.toString());
        }
        return ok;
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
    public boolean save(T obj) {
        return super.save(obj);
    }

    @Override
    public void save(List<T> objs) {
        super.save(objs);
    }

    @Override
    public T get(K primaryKey) {
        T obj = cache.getIfPresent(primaryKey);
        if (obj == null) {
            synchronized (StringUtil.getString(PREFIX, tableMeta.getName(), "get", primaryKey)) {
                obj = cache.getIfPresent(primaryKey);
                if (obj == null) {
                    obj = super.get(primaryKey);
                    if (obj != null) {
                        addCache(primaryKey, obj);
                    } else {
                        addCache(primaryKey, EMPTY);
                    }
                }
            }
        } else if (obj == EMPTY) {
            return null;
        }
        return obj;
    }

    @Override
    public T getOrCreate(K primaryKey, Supplier<T> supplier) {
        T entity = get(primaryKey);
        if (entity == null) {
            synchronized (StringUtil.getString(PREFIX, tableMeta.getName(), "getOrCreate", primaryKey)) {
                entity = get(primaryKey);
                if (entity == null) {
                    entity = supplier.get();
                    add(entity);
                }
            }
        }
        return entity;
    }

    @Override
    public List<T> list(String field, Object value) {
        if (tableMeta.getCacheKeys().contains(tableMeta.getColumnMetaMap().get(field))) {
            return listPrimaryValueByCacheKey(field, value).stream().map(this::get).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return list(ImmutableMap.of(field, value));
    }

    @Override
    public List<T> list(Map<String, Object> optional) {
        LinkedList<Set<K>> pkList = Lists.newLinkedList();
        Map<String, Object> normal = Maps.newHashMap();
        optional.forEach((key, value) -> {
            if (tableMeta.getCacheKeys().contains(tableMeta.getColumnMetaMap().get(key))) {
                pkList.add(listPrimaryValueByCacheKey(key, value));
            } else {
                normal.put(key, value);
            }
        });
        List<T> list;
        if (pkList.isEmpty()) {
            super.listPrimaryKey(optional).forEach(this::get);
            list = cache.asMap().values().stream()
                    .filter(obj -> obj != EMPTY)
                    .collect(Collectors.toList());
        } else {
            if (pkList.size() == 1) {
                list = pkList.getFirst().stream().map(this::get).filter(Objects::nonNull).collect(Collectors.toList());
            } else {
                Set<K> first = Sets.newHashSet(pkList.removeFirst());
                first.removeIf(obj -> {
                    for (Set<K> pk : pkList) {
                        if (!pk.contains(obj)) {
                            return true;
                        }
                    }
                    return false;
                });
                list = first.stream().map(this::get).filter(Objects::nonNull).collect(Collectors.toList());
            }
        }
        if (normal.size() > 0) {
            list = list.stream().filter(obj -> {
                for (Map.Entry<String, Object> entry : normal.entrySet()) {
                    if (!TableValueBuilder.isEqualsColumnValue(tableMeta.getColumnMetaMap().get(entry.getKey()), obj, entry.getValue())) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    public List<T> list() {
        long time = System.currentTimeMillis();
        if (time - expireTime() >= findAllTime.get()) {
            synchronized (StringUtil.getString(PREFIX, tableMeta.getName(), "list")) {
                if (time - expireTime() >= findAllTime.get()) {
                    listPrimaryKey().forEach(this::get);
                }
            }
        }
        findAllTime.set(time);
        return cache.asMap().keySet().stream()
                .map(this::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected Set<K> listPrimaryValueByCacheKey(String column, Object cacheValue) {
        Cache<Object, Set<K>> cachePk = cachePkMap.get(column);
        Set<K> pkSet = cachePk.getIfPresent(cacheValue);
        if (cachePkFinishMap.get(column).getIfPresent(cacheValue) == null || pkSet == null) {
            synchronized (StringUtil.getString(PREFIX, tableMeta.getName(), "CacheKey", column, cacheValue)) {
                pkSet = cachePk.getIfPresent(cacheValue);
                if (pkSet == null) {
                    pkSet = Sets.newConcurrentHashSet();
                    cachePk.put(cacheValue, pkSet);
                }
            }
            cachePkFinishMap.get(column).put(cacheValue, true);
            pkSet.addAll(listPrimaryKey(ImmutableMap.of(column, cacheValue)));
        }
        return pkSet;
    }

    @SuppressWarnings("unchecked")
    protected void addCache(T obj) {
        addCache((K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj), obj);
    }

    protected void addCache(K primaryKey, T obj) {
        synchronized (StringUtil.getString(PREFIX, tableMeta.getName(), "addCache", primaryKey)) {
            T old = cache.getIfPresent(primaryKey);
            if (old == null || old == EMPTY) {
                cache.put(primaryKey, obj);
            }
            if (obj == EMPTY) {
                return;
            }
            tableMeta.getCacheKeys().forEach(columnMeta -> {
                Object cacheValue = TableValueBuilder.getColumnValue(columnMeta, obj);
                Cache<Object, Set<K>> cachePk = cachePkMap.get(columnMeta.getName());
                Set<K> pkSet = cachePk.getIfPresent(cacheValue);
                cachePkFinishMap.get(columnMeta.getName()).getIfPresent(cacheValue);
                if (pkSet == null) {
                    synchronized (StringUtil.getString(PREFIX, tableMeta.getName(), "CacheKey", columnMeta.getName(), cacheValue)) {
                        pkSet = cachePk.getIfPresent(cacheValue);
                        if (pkSet == null) {
                            pkSet = Sets.newConcurrentHashSet();
                            cachePk.put(cacheValue, pkSet);
                        }
                    }
                }
                pkSet.add(primaryKey);
            });
        }
    }

    @SuppressWarnings("unchecked")
    protected void removeCache(T obj) {
        removeCacheByPrimaryKey((K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj));
    }

    protected void removeCacheByPrimaryKey(K primaryKey) {
        synchronized (StringUtil.getString(PREFIX, tableMeta.getName(), "addCache", primaryKey)) {
            cache.put(primaryKey, EMPTY);
            tableMeta.getCacheKeys().forEach(columnMeta ->
                cachePkMap.get(columnMeta.getName()).asMap().values().stream()
                        .filter(Objects::nonNull)
                        .forEach(pkSet -> pkSet.remove(primaryKey)));
        }
    }

    protected boolean addByPrimaryKeyNotAddCache(T obj, K primaryKey) {
        boolean ok = super.addByPrimaryKey(obj, primaryKey);
        if (!ok) {
            log.error("CacheRepository addByPrimaryKeyNotAddCache failure.");
            log.error("{} {}", obj.toString(), primaryKey.toString());
        }
        return ok;
    }

    protected boolean removeByPrimaryKeyNotRemoveCache(K primaryKey) {
        boolean ok = super.removeByPrimaryKey(primaryKey);
        if (!ok) {
            log.error("CacheRepository removeByPrimaryKeyNotRemoveCache failure.");
            log.error(primaryKey.toString());
        }
        return ok;
    }

}

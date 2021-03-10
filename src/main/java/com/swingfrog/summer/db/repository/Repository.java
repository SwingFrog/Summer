package com.swingfrog.summer.db.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Repository<T, K> {

    T add(T obj);
    boolean remove(T obj);
    boolean removeByPrimaryKey(K primaryKey);
    void removeAll();
    boolean save(T obj);
    void save(Collection<T> objs);
    void forceSave(T obj);
    T get(K primaryKey);
    T getOrCreate(K primaryKey, Supplier<T> supplier);
    List<T> list(String field, Object value);
    List<T> list(String field, Object value, Predicate<T> filter);
    List<T> list(Map<String, Object> optional);
    List<T> list(Map<String, Object> optional, Predicate<T> filter);
    default List<T> list() {
        return listAll();
    }
    List<T> listAll();
    List<T> listAll(Predicate<T> filter);
    List<T> listSingleCache(Object value);
    List<T> listSingleCache(Object value, Predicate<T> filter);
    Class<T> getEntityClass();

}

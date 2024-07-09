package com.swingfrog.summer.db.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.swingfrog.summer.db.BaseDao;
import com.swingfrog.summer.db.DaoRuntimeException;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RepositoryDao<T, K> extends BaseDao<T> implements Repository<T, K> {

    private final BeanHandler<T> beanHandler;
    private final BeanListHandler<T> beanListHandler;

    private String replaceSql;
    private String deleteSql;
    private String deleteAllSql;
    private String updateSql;
    private String selectSql;
    private String selectAllSql;
    private AtomicLong primaryKey;
    private TableMeta tableMeta;
    private String singeCacheField;

    private Set<String> shardingTableNames;

    protected RepositoryDao() {
        super();
        Class<T> entityClass = getEntityClass();
        beanHandler = new BeanHandler<>(entityClass, new BasicRowProcessor(new RepositoryBeanProcessor()));
        beanListHandler = new BeanListHandler<>(entityClass, new BasicRowProcessor(new RepositoryBeanProcessor()));
    }

    void init() {
        TableMeta tableMeta = TableMetaBuilder.getTableMeta(getEntityClass());
        if (tableMeta.getShardingKeys().isEmpty()) {
            init(tableMeta);
        } else {
            initSharding(tableMeta);
        }
        this.tableMeta = tableMeta;
        singeCacheField = tableMeta.getCacheKeys().stream()
                .map(TableMeta.ColumnMeta::getField)
                .map(Field::getName)
                .findAny()
                .orElse(null);
    }

    private void init(TableMeta tableMeta) {
        String tableName = getValue(SqlBuilder.getTableExists(tableMeta.getName()));
        if (tableName == null) {
            update(SqlBuilder.getCreateTable(tableMeta));
        } else {
            List<String> columns = listValue(SqlBuilder.getTableColumn(tableMeta.getName()));
            tableMeta.getColumns().stream()
                    .filter(columnMeta -> !columns.contains(columnMeta.getName()))
                    .forEach(columnMeta -> {
                        update(SqlBuilder.getAddColumn(tableMeta, columnMeta));
                        if (tableMeta.getIndexKeys().contains(columnMeta)) {
                            update(SqlBuilder.getAddColumnIndex(tableMeta, columnMeta));
                        }
                    });
        }
        if (tableMeta.getPrimaryColumn().isAuto()) {
            Object maxPk = getValue(SqlBuilder.getMaxPrimaryKey(tableMeta));
            if (maxPk == null) {
                primaryKey = new AtomicLong(autoIncrement());
            } else {
                primaryKey = new AtomicLong(Long.parseLong(maxPk.toString()));
            }
        }
        replaceSql = SqlBuilder.getReplace(tableMeta);
        deleteSql = SqlBuilder.getDelete(tableMeta);
        deleteAllSql = SqlBuilder.getDeleteAll(tableMeta);
        updateSql = SqlBuilder.getUpdate(tableMeta);
        selectSql = SqlBuilder.getSelect(tableMeta);
        selectAllSql = SqlBuilder.getSelectAll(tableMeta);
    }

    private void initSharding(TableMeta tableMeta) {
        List<String> tableNames = listValue(SqlBuilder.getTableExistsList(tableMeta.getName()));
        shardingTableNames = Sets.newConcurrentHashSet();
        shardingTableNames.addAll(tableNames);
        for (String tableName : tableNames) {
            List<String> columns = listValue(SqlBuilder.getTableColumn(tableName));
            tableMeta.getColumns().stream()
                    .filter(columnMeta -> !columns.contains(columnMeta.getName()))
                    .forEach(columnMeta -> {
                        update(SqlBuilder.getAddColumn(columnMeta, tableName));
                        if (tableMeta.getIndexKeys().contains(columnMeta)) {
                            update(SqlBuilder.getAddColumnIndex(columnMeta, tableName));
                        }
                    });
        }
        if (tableMeta.getPrimaryColumn().isAuto()) {
            Long pk = null;
            for (String tableName : tableNames) {
                Object maxPk = getValue(SqlBuilder.getMaxPrimaryKey(tableMeta, tableName));
                if (maxPk == null) {
                    continue;
                }
                long temp = Long.parseLong(maxPk.toString());
                if (pk == null || temp > pk) {
                    pk = temp;
                }
            }
            if (pk == null) {
                primaryKey = new AtomicLong(autoIncrement());
            } else {
                primaryKey = new AtomicLong(pk);
            }
        }
        replaceSql = SqlBuilder.getReplace(tableMeta, "?");
        deleteSql = SqlBuilder.getDelete(tableMeta, "?");
        deleteAllSql = SqlBuilder.getDeleteAll("?");
        updateSql = SqlBuilder.getUpdate(tableMeta, "?");
        selectSql = SqlBuilder.getSelect(tableMeta, "?");
        selectAllSql = SqlBuilder.getSelectAll("?");
    }

    protected boolean isSharding() {
        return shardingTableNames != null;
    }

    protected boolean isAutoIncrement() {
        return primaryKey != null;
    }

    protected long autoIncrement() {
        return 0;
    }

    private long nextPrimaryKey() {
        if (!isAutoIncrement()) {
            throw new DaoRuntimeException("primary key must be auto increment");
        }
        return primaryKey.incrementAndGet();
    }

    protected T addNotAutoIncrement(T obj) {
        return doAdd(replaceSql, obj, null);
    }

    protected T addByPrimaryKey(T obj, K primaryKey) {
        return doAdd(replaceSql, obj, primaryKey);
    }

    T doAdd(String addSql, T obj, @Nullable K primaryKey) {
        onSaveBefore(obj);
        if (isSharding()) {
            String shardingTableNameValue = TableValueBuilder.getShardingTableNameValue(tableMeta, obj);
            if (shardingTableNameValue == null) {
                throw new DaoRuntimeException("sharding filed value is null");
            }
            if (!shardingTableNames.contains(shardingTableNameValue)) {
                update(SqlBuilder.getCreateTable(tableMeta), shardingTableNameValue);
                shardingTableNames.add(shardingTableNameValue);
            }
            if (update(replaceSql, TableValueBuilder.listInsertValue(tableMeta, obj, primaryKey, shardingTableNameValue)) > 0)
                return obj;
            return null;
        }
        if (update(addSql, TableValueBuilder.listInsertValue(tableMeta, obj, primaryKey, null)) > 0)
            return obj;
        return null;
    }

    protected void autoIncrementPrimaryKey(T obj) {
        if (isAutoIncrement()) {
            TableValueBuilder.setPrimaryKeyIntNumberValue(tableMeta, obj, nextPrimaryKey());
        }
    }

    @Override
    public T add(T obj) {
        Objects.requireNonNull(obj);
        autoIncrementPrimaryKey(obj);
        return addNotAutoIncrement(obj);
    }

    @Override
    public boolean remove(T obj) {
        Objects.requireNonNull(obj);
        if (isSharding()) {
            String shardingTableNameValue = TableValueBuilder.getShardingTableNameValue(tableMeta, obj);
            if (shardingTableNameValue == null) {
                throw new DaoRuntimeException("sharding filed value is null");
            }
            return update(deleteSql, shardingTableNameValue, TableValueBuilder.getPrimaryKeyValue(tableMeta, obj)) > 0;
        }
        return update(deleteSql, TableValueBuilder.getPrimaryKeyValue(tableMeta, obj)) > 0;
    }

    @Override
    public boolean removeByPrimaryKey(K primaryKey) {
        Objects.requireNonNull(primaryKey);
        if (isSharding()) {
            for (String shardingTableName : shardingTableNames) {
                if (update(deleteSql, shardingTableName, primaryKey) > 0) {
                    return true;
                }
            }
            return false;
        }
        return update(deleteSql, primaryKey) > 0;
    }

    @Override
    public void removeAll() {
        if (isSharding()) {
            for (String shardingTableName : shardingTableNames) {
                update(deleteAllSql, shardingTableName);
            }
            return;
        }
        update(deleteAllSql);
    }

    @Override
    public boolean save(T obj) {
        Objects.requireNonNull(obj);
        return doSave(obj) > 0;
    }

    @Override
    public void save(Collection<T> objs) {
        Objects.requireNonNull(objs);
        objs.forEach(this::doSave);
    }

    @Override
    public void forceSave(T obj) {
        Objects.requireNonNull(obj);
        doSave(obj);
    }

    int doSave(T obj) {
        onSaveBefore(obj);
        if (isSharding()) {
            String shardingTableNameValue = TableValueBuilder.getShardingTableNameValue(tableMeta, obj);
            if (shardingTableNameValue == null) {
                throw new DaoRuntimeException("sharding filed value is null");
            }
            return update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj, shardingTableNameValue));
        }
        return update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj, null));
    }

    @Override
    public T get(K primaryKey) {
        Objects.requireNonNull(primaryKey);
        if (isSharding()) {
            for (String shardingTableName : shardingTableNames) {
                T value = get(selectSql, shardingTableName, primaryKey);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
        return get(selectSql, primaryKey);
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

    private List<T> doListData(Map<String, Object> optional) {
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        if (isSharding()) {
            String shardingTableNameValue = TableValueBuilder.getShardingTableNameValue(tableMeta, optional);
            if (shardingTableNameValue != null) {
                String sql = SqlBuilder.getSelectField(fields, shardingTableNameValue);
                return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
            }
            List<T> list = Lists.newArrayList();
            for (String shardingTableName : shardingTableNames) {
                String sql = SqlBuilder.getSelectField(fields, shardingTableName);
                list.addAll(list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields)));
            }
            return list;
        }
        String sql = SqlBuilder.getSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
    }

    private List<T> doListPk(Map<String, Object> optional) {
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        if (isSharding()) {
            String shardingTableNameValue = TableValueBuilder.getShardingTableNameValue(tableMeta, optional);
            if (shardingTableNameValue != null) {
                String sql = SqlBuilder.getPrimaryColumnSelectField(tableMeta, fields, shardingTableNameValue);
                return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
            }
            List<T> list = Lists.newArrayList();
            for (String shardingTableName : shardingTableNames) {
                String sql = SqlBuilder.getPrimaryColumnSelectField(tableMeta, fields, shardingTableName);
                list.addAll(list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields)));
            }
            return list;
        }
        String sql = SqlBuilder.getPrimaryColumnSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
    }

    @Override
    public List<T> list(String field, Object value) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(value);
        Map<String, Object> optional = ImmutableMap.of(field, value);
        return doListData(optional);
    }

    @Override
    public List<T> list(String field, Object value, Predicate<T> filter) {
        List<T> list = list(field, value);
        if (filter == null)
            return list;
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<T> list(Map<String, Object> optional) {
        Objects.requireNonNull(optional);
        return doListData(optional);
    }

    @Override
    public List<T> list(Map<String, Object> optional, Predicate<T> filter) {
        List<T> list = list(optional);
        if (filter == null)
            return list;
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<T> listAll() {
        if (isSharding()) {
            List<T> list = Lists.newArrayList();
            for (String shardingTableName : shardingTableNames) {
                list.addAll(list(selectAllSql, shardingTableName));
            }
            return list;
        }
        return list(selectAllSql);
    }

    @Override
    public List<T> listAll(Predicate<T> filter) {
        List<T> list = listAll();
        if (filter == null)
            return list;
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<T> listSingleCache(Object value) {
        if (singeCacheField == null) {
            throw new DaoRuntimeException("sing cache field not found");
        }
        Objects.requireNonNull(value);
        Map<String, Object> optional = ImmutableMap.of(singeCacheField, value);
        return doListData(optional);
    }

    @Override
    public List<T> listSingleCache(Object value, Predicate<T> filter) {
        return listSingleCache(value).stream().filter(filter).collect(Collectors.toList());
    }

    private T get(String sql, Object... args) {
        return convert(getBean(sql, beanHandler, args));
    }

    private List<T> list(String sql, Object... args) {
        return convert(listBean(sql, beanListHandler, args));
    }

    private T convert(T obj) {
        if (obj == null) {
            return null;
        }
        tableMeta.getColumns().stream()
                .filter(columnMeta -> {
                    try {
                        Object value = columnMeta.getField().get(obj);
                        return value instanceof Collection || value instanceof Map;
                    } catch (IllegalAccessException e) {
                        return false;
                    }
                })
                .forEach(columnMeta -> TableValueBuilder.jsonConvertJavaBean(columnMeta.getField(), obj));
        onLoadAfter(obj);
        return obj;
    }

    private List<T> convert(List<T> objs) {
        objs.forEach(this::convert);
        return objs;
    }

    @SuppressWarnings("unchecked")
    protected List<K> listPrimaryKey(Map<String, Object> optional) {
        return doListPk(optional).stream()
                .map(obj -> (K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected List<K> listPrimaryKey() {
        return list(SqlBuilder.getPrimaryColumnSelectField(tableMeta, ImmutableList.of())).stream()
                .map(obj -> (K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj))
                .collect(Collectors.toList());
    }

    protected TableMeta getTableMeta() {
        return tableMeta;
    }

    protected String getSingeCacheField() {
        return singeCacheField;
    }

    @Override
    public Class<T> getEntityClass() {
        return super.getEntityClass();
    }

    @Override
    public Stream<T> stream(String field, Object value) {
        return list(field, value).stream();
    }

    @Override
    public Stream<T> stream(Map<String, Object> optional) {
        return list(optional).stream();
    }

    @Override
    public Stream<T> streamSingleCache(Object value) {
        return listSingleCache(value).stream();
    }

    @Override
    public Stream<T> streamAll() {
        return listAll().stream();
    }

    protected void onLoadAfter(T obj) {}
    protected void onSaveBefore(T obj) {}

}

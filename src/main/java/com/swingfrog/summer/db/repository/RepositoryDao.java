package com.swingfrog.summer.db.repository;

import com.google.common.collect.*;
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

    private SqlCache sqlCache;
    private AtomicLong primaryKey;
    private TableMeta tableMeta;
    private String singeCacheField;

    private Map<String, SqlCache> shardingSqlCaches;

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
        sqlCache = new SqlCache();
    }

    private void initSharding(TableMeta tableMeta) {
        List<String> tableNames = listValue(SqlBuilder.getTableExistsList(tableMeta.getName()));
        shardingSqlCaches = Maps.newConcurrentMap();
        for (String tableName : tableNames) {
            Long count = getValue(SqlBuilder.getCount(tableName));
            if (count == null || count == 0) {
                update(SqlBuilder.getDropTable(tableName));
                continue;
            }
            shardingSqlCaches.put(tableName, new SqlCache());
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
            for (String tableName : shardingSqlCaches.keySet()) {
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
    }

    protected SqlCache getSqlCache(String tableName) {
        if (isSharding()) {
            SqlCache shardingSqlCache = shardingSqlCaches.get(tableName);
            if (shardingSqlCache == null) {
                shardingSqlCache = new SqlCache();
                SqlCache old = shardingSqlCaches.putIfAbsent(tableName, shardingSqlCache);
                if (old != null) {
                    shardingSqlCache = old;
                }
            }
            return shardingSqlCache;
        }
        return sqlCache;
    }

    protected boolean isSharding() {
        return shardingSqlCaches != null;
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

    protected boolean isUseReplaceSql() {
        return true;
    }

    protected T addNotAutoIncrement(T obj) {
        return doAdd(obj, null);
    }

    protected T addByPrimaryKey(T obj, K primaryKey) {
        return doAdd(obj, primaryKey);
    }

    T doAdd(T obj, @Nullable K primaryKey) {
        onSaveBefore(obj);
        String tableName;
        if (isSharding()) {
            tableName = TableValueBuilder.getShardingTableNameValue(tableMeta, obj);
            if (tableName == null) {
                throw new DaoRuntimeException("sharding filed value is null");
            }
            if (!shardingSqlCaches.containsKey(tableName)) {
                update(SqlBuilder.getCreateTable(tableMeta, tableName));
                shardingSqlCaches.put(tableName, new SqlCache());
            }
        } else {
            tableName = tableMeta.getName();
        }
        String addSql = isUseReplaceSql() ?
                getSqlCache(tableName).getReplace(tableMeta, tableName) :
                getSqlCache(tableName).getInsert(tableMeta, tableName);
        if (update(addSql, TableValueBuilder.listInsertValue(tableMeta, obj, primaryKey)) > 0)
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
        String tableName;
        if (isSharding()) {
            tableName = TableValueBuilder.getShardingTableNameValue(tableMeta, obj);
            if (tableName == null) {
                throw new DaoRuntimeException("sharding filed value is null");
            }
        } else {
            tableName = tableMeta.getName();
        }
        String deleteSql = getSqlCache(tableName).getDelete(tableMeta, tableName);
        return update(deleteSql, TableValueBuilder.getPrimaryKeyValue(tableMeta, obj)) > 0;
    }

    @Override
    public boolean removeByPrimaryKey(K primaryKey) {
        Objects.requireNonNull(primaryKey);
        if (isSharding()) {
            for (Map.Entry<String, SqlCache> entry : shardingSqlCaches.entrySet()) {
                String deleteSql = entry.getValue().getDelete(tableMeta, entry.getKey());
                if (update(deleteSql, primaryKey) > 0) {
                    return true;
                }
            }
            return false;
        }
        String deleteSql = sqlCache.getDelete(tableMeta, tableMeta.getName());
        return update(deleteSql, primaryKey) > 0;
    }

    @Override
    public void removeAll() {
        if (isSharding()) {
            for (Map.Entry<String, SqlCache> entry : shardingSqlCaches.entrySet()) {
                String deleteAllSql = entry.getValue().getDeleteAll(entry.getKey());
                update(deleteAllSql);
            }
            return;
        }
        String deleteAllSql = sqlCache.getDeleteAll(tableMeta.getName());
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
        String tableName;
        if (isSharding()) {
            tableName = TableValueBuilder.getShardingTableNameValue(tableMeta, obj);
            if (tableName == null) {
                throw new DaoRuntimeException("sharding filed value is null");
            }
        } else {
            tableName = tableMeta.getName();
        }
        String updateSql = getSqlCache(tableName).getUpdate(tableMeta, tableName);
        return update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj));
    }

    @Override
    public T get(K primaryKey) {
        Objects.requireNonNull(primaryKey);
        if (isSharding()) {
            for (Map.Entry<String, SqlCache> entry : shardingSqlCaches.entrySet()) {
                String selectSql = entry.getValue().getSelect(tableMeta, entry.getKey());
                T value = get(selectSql, primaryKey);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
        String selectSql = sqlCache.getSelect(tableMeta, tableMeta.getName());
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
                if (!shardingSqlCaches.containsKey(shardingTableNameValue)) {
                    return Lists.newArrayList();
                }
                String sql = SqlBuilder.getSelectField(fields, shardingTableNameValue);
                return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
            }
            List<T> list = Lists.newArrayList();
            for (String shardingTableName : shardingSqlCaches.keySet()) {
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
                if (!shardingSqlCaches.containsKey(shardingTableNameValue)) {
                    return Lists.newArrayList();
                }
                String sql = SqlBuilder.getPrimaryColumnSelectField(tableMeta, fields, shardingTableNameValue);
                return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
            }
            List<T> list = Lists.newArrayList();
            for (String shardingTableName : shardingSqlCaches.keySet()) {
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
            for (Map.Entry<String, SqlCache> entry : shardingSqlCaches.entrySet()) {
                String selectAllSql = entry.getValue().getSelectAll(entry.getKey());
                list.addAll(list(selectAllSql));
            }
            return list;
        }
        String selectAllSql = sqlCache.getSelectAll(tableMeta.getName());
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
        if (isSharding()) {
            List<K> list = Lists.newArrayList();
            for (String tableName : shardingSqlCaches.keySet()) {
                list(SqlBuilder.getPrimaryColumnSelectField(tableMeta, ImmutableList.of(), tableName))
                        .stream()
                        .map(obj -> (K) TableValueBuilder.getPrimaryKeyValue(tableMeta, obj))
                        .forEach(list::add);
            }
            return list;
        }
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

package com.swingfrog.summer.db.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.db.BaseDao;
import com.swingfrog.summer.db.DaoRuntimeException;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    protected RepositoryDao() {
        super();
        beanHandler = new BeanHandler<>(getEntityClass(), new BasicRowProcessor(new RepositoryBeanProcessor()));
        beanListHandler = new BeanListHandler<>(getEntityClass(), new BasicRowProcessor(new RepositoryBeanProcessor()));
    }

    void init() {
        TableMeta tableMeta = TableMetaBuilder.getTableMeta(getEntityClass());
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
        this.tableMeta = tableMeta;
        singeCacheField = tableMeta.getCacheKeys().stream()
                .map(TableMeta.ColumnMeta::getField)
                .map(Field::getName)
                .findAny()
                .orElse(null);
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
        if (update(replaceSql, TableValueBuilder.listInsertValue(tableMeta, obj)) > 0)
            return obj;
        return null;
    }

    protected T addByPrimaryKey(T obj, K primaryKey) {
        if (update(replaceSql, TableValueBuilder.listInsertValue(tableMeta, obj, primaryKey)) > 0)
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
        return update(deleteSql, TableValueBuilder.getPrimaryKeyValue(tableMeta, obj)) > 0;
    }

    @Override
    public boolean removeByPrimaryKey(K primaryKey) {
        Objects.requireNonNull(primaryKey);
        return update(deleteSql, primaryKey) > 0;
    }

    @Override
    public void removeAll() {
        update(deleteAllSql);
    }

    @Override
    public boolean save(T obj) {
        Objects.requireNonNull(obj);
        return update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj)) > 0;
    }

    @Override
    public void save(Collection<T> objs) {
        Objects.requireNonNull(objs);
        objs.forEach(obj -> update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj)));
    }

    @Override
    public void forceSave(T obj) {
        Objects.requireNonNull(obj);
        update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj));
    }

    @Override
    public T get(K primaryKey) {
        Objects.requireNonNull(primaryKey);
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

    @Override
    public List<T> list(String field, Object value) {
        Objects.requireNonNull(field);
        Objects.requireNonNull(value);
        Map<String, Object> optional = ImmutableMap.of(field, value);
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        String sql = SqlBuilder.getSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
    }

    @Override
    public List<T> list(String field, Object value, Predicate<T> filter) {
        return list(field, value).stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<T> list(Map<String, Object> optional) {
        Objects.requireNonNull(optional);
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        String sql = SqlBuilder.getSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
    }

    @Override
    public List<T> list(Map<String, Object> optional, Predicate<T> filter) {
        return list(optional).stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<T> list() {
        return list(selectAllSql);
    }

    @Override
    public List<T> listSingleCache(Object value) {
        if (singeCacheField == null) {
            throw new DaoRuntimeException("sing cache field not found");
        }
        Objects.requireNonNull(value);
        Map<String, Object> optional = ImmutableMap.of(singeCacheField, value);
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        String sql = SqlBuilder.getSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
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
        return obj;
    }

    private List<T> convert(List<T> objs) {
        objs.forEach(this::convert);
        return objs;
    }

    @SuppressWarnings("unchecked")
    protected List<K> listPrimaryKey(Map<String, Object> optional) {
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        String sql = SqlBuilder.getPrimaryColumnSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields)).stream()
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

}

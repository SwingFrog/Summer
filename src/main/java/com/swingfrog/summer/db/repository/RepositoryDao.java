package com.swingfrog.summer.db.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.swingfrog.summer.db.BaseDao;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public abstract class RepositoryDao<T, K> extends BaseDao<T> {

    private BeanHandler<T> beanHandler;
    private BeanListHandler<T> beanListHandler;

    private String insertSql;
    private String deleteSql;
    private String updateSql;
    private String selectSql;
    private String selectAllSql;
    private AtomicLong primaryKey;
    protected TableMeta tableMeta;

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
        insertSql = SqlBuilder.getInsert(tableMeta);
        deleteSql = SqlBuilder.getDelete(tableMeta);
        updateSql = SqlBuilder.getUpdate(tableMeta);
        selectSql = SqlBuilder.getSelect(tableMeta);
        selectAllSql = SqlBuilder.getSelectAll(tableMeta);
        this.tableMeta = tableMeta;
    }

    protected boolean isAutoIncrement() {
        return primaryKey != null;
    }

    protected long autoIncrement() {
        return 0;
    }

    private long nextPrimaryKey() {
        if (!isAutoIncrement()) {
            throw new UnsupportedOperationException("primary key must be auto increment");
        }
        return primaryKey.incrementAndGet();
    }

    protected boolean addNotAutoIncrement(T obj) {
        Objects.requireNonNull(obj, "repository add param not null");
        return update(insertSql, TableValueBuilder.listInsertValue(tableMeta, obj)) > 0;
    }

    protected boolean addByPrimaryKey(T obj, K primaryKey) {
        Objects.requireNonNull(obj, "repository add param not null");
        Objects.requireNonNull(primaryKey, "repository add param not null");
        return update(insertSql, TableValueBuilder.listInsertValue(tableMeta, obj, primaryKey)) > 0;
    }

    protected void autoIncrementPrimaryKey(T obj) {
        Objects.requireNonNull(obj, "repository auto increment primary key param not null");
        if (isAutoIncrement()) {
            TableValueBuilder.setPrimaryKeyIntNumberValue(tableMeta, obj, nextPrimaryKey());
        }
    }

    public boolean add(T obj) {
        Objects.requireNonNull(obj, "repository add param not null");
        autoIncrementPrimaryKey(obj);
        return addNotAutoIncrement(obj);
    }

    public boolean remove(T obj) {
        Objects.requireNonNull(obj, "repository remove param not null");
        return update(deleteSql, TableValueBuilder.getPrimaryKeyValue(tableMeta, obj)) > 0;
    }

    public boolean removeByPrimaryKey(K primaryKey) {
        Objects.requireNonNull(primaryKey, "repository remove param not null");
        return update(deleteSql, primaryKey) > 0;
    }

    public boolean save(T obj) {
        Objects.requireNonNull(obj, "repository save param not null");
        return update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj)) > 0;
    }

    public void save(List<T> objs) {
        Objects.requireNonNull(objs, "repository save param not null");
        objs.forEach(obj -> update(updateSql, TableValueBuilder.listUpdateValue(tableMeta, obj)));
    }

    public T get(K primaryKey) {
        Objects.requireNonNull(primaryKey, "repository get primary key not null");
        return get(selectSql, primaryKey);
    }

    public List<T> list(String field, Object value) {
        Objects.requireNonNull(field, "repository list field not null");
        Objects.requireNonNull(value, "repository list value not null");
        Map<String, Object> optional = ImmutableMap.of(field, value);
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        String sql = SqlBuilder.getSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
    }

    public List<T> list(Map<String, Object> optional) {
        Objects.requireNonNull(optional, "repository list optional not null");
        List<String> fields = TableValueBuilder.listValidFieldByOptional(tableMeta, optional);
        String sql = SqlBuilder.getSelectField(tableMeta, fields);
        return list(sql, TableValueBuilder.listValidValueByOptional(tableMeta, optional, fields));
    }

    public List<T> list() {
        return list(selectAllSql);
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

}

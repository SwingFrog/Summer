package com.swingfrog.summer.db.repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

public class TableMetaBuilder {

    public static TableMeta getTableMeta(Class<?> clazz) {
        TableMeta tableMeta = new TableMeta();
        Table table = clazz.getAnnotation(Table.class);
        Objects.requireNonNull(table, String.format("entity need use @Table - %s", clazz.getName()));
        if (table.name().length() == 0) {
            tableMeta.setName(clazz.getSimpleName());
        } else {
            tableMeta.setName(table.name());
        }
        if (table.charset().length() > 0) {
            tableMeta.setCharset(table.charset());
            if (table.collate().length() > 0) {
                tableMeta.setCollate(table.collate());
            }
        }
        tableMeta.setComment(table.comment());
        tableMeta.setColumns(Lists.newLinkedList());
        tableMeta.setIndexKeys(Sets.newHashSet());
        tableMeta.setCacheKeys(Sets.newHashSet());
        List<Field> fields = Lists.newLinkedList();
        collectField(fields, clazz);
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                field.setAccessible(true);
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                if (primaryKey != null) {
                    if (tableMeta.getPrimaryColumn() != null) {
                        throw new RuntimeException(String.format("only one primary key - %s", clazz.getName()));
                    }
                    tableMeta.setPrimaryColumn(getColumnMate(field));
                    if (TableSupport.isJavaBean(tableMeta.getPrimaryColumn())) {
                        throw new RuntimeException("primary key type not be java bean");
                    }
                } else {
                    TableMeta.ColumnMeta columnMeta = getColumnMate(field);
                    tableMeta.getColumns().add(columnMeta);
                    IndexKey indexKey = field.getAnnotation(IndexKey.class);
                    if (indexKey != null) {
                        tableMeta.getIndexKeys().add(columnMeta);
                    }
                    CacheKey cacheKey = field.getAnnotation(CacheKey.class);
                    if (cacheKey != null) {
                        tableMeta.getCacheKeys().add(columnMeta);
                    }
                }
            }
        }
        Map<String, TableMeta.ColumnMeta> columnMetaMap = Maps.newHashMap();
        columnMetaMap.put(tableMeta.getPrimaryColumn().getName(), tableMeta.getPrimaryColumn());
        tableMeta.getColumns().forEach(columnMeta -> columnMetaMap.put(columnMeta.getName(), columnMeta));
        tableMeta.setColumnMetaMap(columnMetaMap);
        return tableMeta;
    }

    private static void collectField(List<Field> list, Class<?> entityClass) {
        if (entityClass == null)
            return;
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                list.add(field);
            }
        }
        collectField(list, entityClass.getSuperclass());
    }

    private static TableMeta.ColumnMeta getColumnMate(Field field) {
        TableMeta.ColumnMeta columnMeta = new TableMeta.ColumnMeta();
        Column column = field.getAnnotation(Column.class);
        columnMeta.setName(field.getName());
        ColumnType columnType;
        if (column.type() == ColumnType.DEFAULT) {
            columnType = getColumnType(field, column);
            if (columnType == ColumnType.CHAR || columnType == ColumnType.VARCHAR) {
                columnMeta.setType(String.format("%s(%s)", columnType.name(), column.length()));
            } else {
                columnMeta.setType(columnType.name());
            }
        } else {
            columnType = column.type();
            columnMeta.setType(column.type().name());
        }
        if (columnMeta.getType().toUpperCase().endsWith("INT")) {
            columnMeta.setIntNumber(true);
        }
        columnMeta.setReadOnly(column.readOnly());
        columnMeta.setField(field);
        columnMeta.setNonNull(column.nonNull());
        columnMeta.setUnsigned(column.unsigned());
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        if (primaryKey != null) {
            if (columnMeta.isIntNumber()) {
                columnMeta.setAuto(primaryKey.auto());
            }
            columnMeta.setNonNull(true);
        }
        IndexKey indexKey = field.getAnnotation(IndexKey.class);
        if (indexKey != null) {
            columnMeta.setIndex(indexKey.index());
        }
        columnMeta.setComment(column.comment());

        columnMeta.setDefaultValue(getDefaultValue(field, columnType));
        return columnMeta;
    }

    private static ColumnType getColumnType(Field field, Column column) {
        Class<?> type = field.getType();
        if (type == boolean.class || type == Boolean.class)
            return ColumnType.TINYINT;
        if (type == byte.class || type == Byte.class)
            return ColumnType.TINYINT;
        if (type == short.class || type == Short.class)
            return ColumnType.SMALLINT;
        if (type == int.class || type == Integer.class)
            return ColumnType.INT;
        if (type == long.class || type == Long.class)
            return ColumnType.BIGINT;
        if (type == float.class || type == Float.class)
            return ColumnType.FLOAT;
        if (type == double.class || type == Double.class)
            return ColumnType.DOUBLE;
        if (type == Date.class || type == LocalDateTime.class)
            return ColumnType.DATETIME;
        if (type == Enum.class)
            return ColumnType.INT;
        if (type.isArray() && type.getComponentType() == Byte.class)
            return ColumnType.BLOB;
        int length = column.length();
        if (length <= 255)
            return ColumnType.CHAR;
        if (length <= 16383)
            return ColumnType.VARCHAR;
        return ColumnType.TEXT;
    }

    private static String getDefaultValue(Field field, ColumnType columnType) {
        if (columnType == ColumnType.BLOB || columnType == ColumnType.LONGBLOB)
            return null;
        if (columnType == ColumnType.TEXT || columnType == ColumnType.LONGTEXT)
            return null;
        Class<?> type = field.getType();
        if (type == boolean.class)
            return "0";
        if (type == byte.class)
            return "0";
        if (type == short.class)
            return "0";
        if (type == int.class)
            return "0";
        if (type == long.class)
            return "0";
        if (type == float.class)
            return "0";
        if (type == double.class)
            return "0";
        if (type.isArray() || Collection.class.isAssignableFrom(type))
            return "[]";
        if (Map.class.isAssignableFrom(type))
            return "{}";
        return null;
    }
}

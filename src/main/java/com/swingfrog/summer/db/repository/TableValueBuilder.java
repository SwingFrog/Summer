package com.swingfrog.summer.db.repository;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableValueBuilder {

    private static final Logger log = LoggerFactory.getLogger(TableValueBuilder.class);

    @FunctionalInterface
    interface ThreeConsumer<T, U, O> {
        void accept(T t, U u, O o);
    }

    private static final Map<Type, ThreeConsumer<Field, Object, Long>> primaryFieldMap = Maps.newHashMap();
    static {
        ThreeConsumer<Field, Object, Long> fieldLong = (field, obj, primaryValue) -> {
            try {
                field.setLong(obj, primaryValue);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        };
        ThreeConsumer<Field, Object, Long> fieldInt = (field, obj, primaryValue) -> {
            try {
                field.setInt(obj, primaryValue.intValue());
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        };
        ThreeConsumer<Field, Object, Long> fieldShort = (field, obj, primaryValue) -> {
            try {
                field.setShort(obj, primaryValue.shortValue());
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        };
        ThreeConsumer<Field, Object, Long> fieldByte = (field, obj, primaryValue) -> {
            try {
                field.setByte(obj, primaryValue.byteValue());
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        };
        primaryFieldMap.put(long.class, fieldLong);
        primaryFieldMap.put(Long.class, fieldLong);
        primaryFieldMap.put(int.class, fieldInt);
        primaryFieldMap.put(Integer.class, fieldInt);
        primaryFieldMap.put(short.class, fieldShort);
        primaryFieldMap.put(Short.class, fieldShort);
        primaryFieldMap.put(byte.class, fieldByte);
        primaryFieldMap.put(Byte.class, fieldByte);
    }

    public static Object convert(Object obj, Type target) {
        if (TableSupport.isJavaBean(target)) {
            return JSON.toJSONString(obj);
        } else {
            return obj;
        }
    }

    public static Object getFieldValue(Field field, Object obj) {
        Object res = null;
        try {
            res = field.get(obj);
            res = convert(res, field.getGenericType());
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }

    public static void jsonConvertJavaBean(Field field, Object obj) {
        try {
            field.set(obj, JSON.parseObject(JSON.toJSONString(field.get(obj)), field.getGenericType()));
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static boolean isEqualsColumnValue(TableMeta.ColumnMeta columnMeta, Object obj, Object value) {
        Object columnValue = getColumnValue(columnMeta, obj);
        if (columnValue.equals(value)) {
            return true;
        }
        return columnValue.toString().equals(value.toString());
    }

    public static Object getColumnValue(TableMeta.ColumnMeta columnMeta, Object obj) {
        return getFieldValue(columnMeta.getField(), obj);
    }

    public static Object getPrimaryKeyValue(TableMeta tableMeta, Object obj) {
        return getColumnValue(tableMeta.getPrimaryColumn(), obj);
    }

    @Nullable
    public static String getShardingTableNameValue(TableMeta tableMeta, Object obj) {
        List<TableMeta.ColumnMeta> shardingKeys = tableMeta.getShardingKeys();
        StringBuilder builder = null;
        for (TableMeta.ColumnMeta shardingKey : shardingKeys) {
            Object value = getColumnValue(shardingKey, obj);
            if (value == null) {
                return null;
            }
            if (builder == null) {
                builder = new StringBuilder();
                builder.append(tableMeta.getName());
            }
            builder.append("_").append(value);
        }
        if (builder == null) {
            return null;
        }
        return builder.toString();
    }

    @Nullable
    public static String getShardingTableNameValue(TableMeta tableMeta, Map<String, Object> optional) {
        List<TableMeta.ColumnMeta> shardingKeys = tableMeta.getShardingKeys();
        StringBuilder builder = null;
        for (TableMeta.ColumnMeta shardingKey : shardingKeys) {
            Object value = optional.get(shardingKey.getName());
            if (value == null) {
                return null;
            }
            if (builder == null) {
                builder = new StringBuilder();
                builder.append(tableMeta.getName());
            }
            builder.append("_").append(value);
        }
        if (builder == null) {
            return null;
        }
        return builder.toString();
    }

    public static void setPrimaryKeyIntNumberValue(TableMeta tableMeta, Object obj, long primaryValue) {
        Field field = tableMeta.getPrimaryColumn().getField();
        Type type = field.getGenericType();
        ThreeConsumer<Field, Object, Long> consumer = primaryFieldMap.get(type);
        if (consumer != null) {
            consumer.accept(field, obj, primaryValue);
        } else {
            throw new UnsupportedOperationException("primary key must be number");
        }
    }

    public static List<String> listValidFieldByOptional(TableMeta tableMeta, Map<String, Object> optional) {
        return optional.keySet().stream().filter(tableMeta.getColumnMetaMap()::containsKey).collect(Collectors.toList());
    }

    public static Object[] listValidValueByOptional(TableMeta tableMeta, Map<String, Object> optional, List<String> fields) {
        return fields.stream().map(field ->
                convert(optional.get(field),
                        tableMeta.getColumnMetaMap().get(field).getField().getGenericType()))
                .toArray();
    }

    public static Object[] listUpdateValue(TableMeta tableMeta, Object obj) {
        int size = tableMeta.getColumns().size();
        List<Object> list = Lists.newArrayListWithCapacity(size);
        tableMeta.getColumns().stream()
                .filter(columnMeta -> !columnMeta.isReadOnly())
                .map(columnMeta -> getColumnValue(columnMeta, obj))
                .forEach(list::add);
        list.add(getPrimaryKeyValue(tableMeta, obj));
        return list.toArray();
    }

    public static Object[] listInsertValue(TableMeta tableMeta, Object obj,
                                           @Nullable Object primaryKey) {
        int size = tableMeta.getColumns().size() + 1;
        List<Object> list = Lists.newArrayListWithCapacity(size);
        if (primaryKey == null) {
            primaryKey = getPrimaryKeyValue(tableMeta, obj);
        }
        list.add(primaryKey);
        tableMeta.getColumns().stream()
                .map(columnMeta -> getColumnValue(columnMeta, obj))
                .forEach(list::add);
        return list.toArray();
    }

}
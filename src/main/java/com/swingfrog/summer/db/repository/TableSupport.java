package com.swingfrog.summer.db.repository;

import com.google.common.collect.Sets;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Set;

public class TableSupport {

    private static final Set<Type> notJavaBeanSet = Sets.newHashSet(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            String.class, Date.class);

    public static boolean isJavaBean(Type type) {
        return !notJavaBeanSet.contains(type);
    }

    public static boolean isJavaBean(TableMeta.ColumnMeta columnMeta) {
        return isJavaBean(columnMeta.getField().getGenericType());
    }

}

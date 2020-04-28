package com.swingfrog.summer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class ConfigUtil {

    private static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    public static void loadDataWithBean(Properties pro, String prefix, Object dest) {
        Class<?> destClass = dest.getClass();
        Field[] destFields = destClass.getDeclaredFields();
        Field destField;
        Method[] destMethods = new Method[destFields.length];
        for (int i = 0; i < destFields.length; i++) {
            destField = destFields[i];
            if (destField != null) {
                try {
                    destMethods[i] = new PropertyDescriptor(destField.getName(), destClass).getWriteMethod();
                } catch (IntrospectionException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        try {
            for (int i = 0; i < destFields.length; i++) {
                destMethods[i].invoke(dest, (Object)getValueByTypeAndString(destFields[i].getType(), pro.getProperty(prefix + destFields[i].getName())));
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValueByTypeAndString(Class<?> clazz, String value) {
        if (clazz == byte.class || clazz == short.class || clazz == int.class || clazz == long.class ||
                clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class) {
            return (T)Integer.valueOf(value);
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return (T)Boolean.valueOf(value);
        } else if (clazz == String[].class) {
            return (T)value.split(",");
        } else if (clazz == byte[].class || clazz == short[].class || clazz == int[].class || clazz == long[].class ||
                clazz == Byte[].class || clazz == Short[].class || clazz == Integer[].class || clazz == Long[].class) {
            String[] strs = value.split(",");
            Integer[] values = new Integer[strs.length];
            for (int i = 0; i < strs.length ; i ++) {
                values[i] = Integer.valueOf(strs[i]);
            }
            return (T)values;
        } else if (clazz == boolean[].class || clazz == Boolean[].class) {
            String[] strs = value.split(",");
            Boolean[] values = new Boolean[strs.length];
            for (int i = 0; i < strs.length ; i ++) {
                values[i] = Boolean.valueOf(strs[i]);
            }
            return (T)values;
        }
        return (T)value;
    }

}

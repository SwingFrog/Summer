package com.swingfrog.summer.config;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

public class ConfigUtil {

    public static void loadDataWithBean(Properties pro, String prefix, Object dest) throws IntrospectionException {
        Class<?> destClass = dest.getClass();
        Field[] destFields = destClass.getDeclaredFields();
        Field destField;
        Method[] destMethods = new Method[destFields.length];
        for (int i = 0; i < destFields.length; i++) {
            destField = destFields[i];
            if (destField != null) {
                destMethods[i] = new PropertyDescriptor(destField.getName(), destClass).getWriteMethod();
            }
        }
        for (int i = 0; i < destFields.length; i++) {
            try {
                destMethods[i].invoke(dest, (Object)getValueByTypeAndString(destFields[i].getType(), pro.getProperty(prefix + destFields[i].getName())));
            } catch (Exception e) {
                throw new RuntimeException(String.format("load properties %s failure", prefix + destFields[i].getName()));
            }

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
            String[] texts = value.split(",");
            Integer[] values = new Integer[texts.length];
            for (int i = 0; i < texts.length ; i ++) {
                values[i] = Integer.valueOf(texts[i]);
            }
            return (T)values;
        } else if (clazz == boolean[].class || clazz == Boolean[].class) {
            String[] texts = value.split(",");
            Boolean[] values = new Boolean[texts.length];
            for (int i = 0; i < texts.length ; i ++) {
                values[i] = Boolean.valueOf(texts[i]);
            }
            return (T)values;
        }
        return (T)value;
    }

}

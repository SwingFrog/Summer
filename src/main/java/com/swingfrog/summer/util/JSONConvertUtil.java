package com.swingfrog.summer.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;

public class JSONConvertUtil {

    private static final Map<Type, BiFunction<JSONObject, String, Object>> map = Maps.newHashMap();
    static {
        map.put(boolean.class, JSONObject::getBooleanValue);
        map.put(byte.class, JSONObject::getByteValue);
        map.put(short.class, JSONObject::getShortValue);
        map.put(int.class, JSONObject::getIntValue);
        map.put(long.class, JSONObject::getLongValue);
        map.put(float.class, JSONObject::getFloatValue);
        map.put(double.class, JSONObject::getDoubleValue);
        map.put(Boolean.class, JSONObject::getBoolean);
        map.put(Byte.class, JSONObject::getByte);
        map.put(Short.class, JSONObject::getShort);
        map.put(Integer.class, JSONObject::getInteger);
        map.put(Long.class, JSONObject::getLong);
        map.put(String.class, JSONObject::getString);
        map.put(Float.class, JSONObject::getFloat);
        map.put(Double.class, JSONObject::getDouble);
    }

    public static boolean containsType(Type type) {
        return map.containsKey(type);
    }

    public static Object convert(Type type, JSONObject jsonObject, String key) {
        return map.get(type).apply(jsonObject, key);
    }

}

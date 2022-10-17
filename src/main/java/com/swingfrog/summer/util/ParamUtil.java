package com.swingfrog.summer.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Defaults;
import com.google.common.collect.Maps;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

public class ParamUtil {

	private static final Map<Type, Function<String, Object>> map = Maps.newHashMap();

	static {
		map.put(boolean.class, text -> {
			if (text == null || text.isEmpty()) {
				return Defaults.defaultValue(Boolean.TYPE);
			}
			try {
				return Boolean.parseBoolean(text);
			} catch (Exception e) {
				return Defaults.defaultValue(Boolean.TYPE);
			}
		});
		map.put(byte.class, text -> {
			if (text == null || text.isEmpty()) {
				return Defaults.defaultValue(Byte.TYPE);
			}
			try {
				return Byte.parseByte(text);
			} catch (Exception e) {
				return Defaults.defaultValue(Byte.TYPE);
			}
		});
		map.put(short.class, text -> {
			if (text == null || text.isEmpty()) {
				return Defaults.defaultValue(Short.TYPE);
			}
			try {
				return Short.parseShort(text);
			} catch (Exception e) {
				return Defaults.defaultValue(Short.TYPE);
			}
		});
		map.put(int.class, text -> {
			if (text == null || text.isEmpty()) {
				return Defaults.defaultValue(Integer.TYPE);
			}
			try {
				return Integer.parseInt(text);
			} catch (Exception e) {
				return Defaults.defaultValue(Integer.TYPE);
			}
		});
		map.put(long.class, text -> {
			if (text == null || text.isEmpty()) {
				return Defaults.defaultValue(Long.TYPE);
			}
			try {
				return Long.parseLong(text);
			} catch (Exception e) {
				return Defaults.defaultValue(Long.TYPE);
			}
		});
		map.put(float.class, text -> {
			if (text == null || text.isEmpty()) {
				return Defaults.defaultValue(Float.TYPE);
			}
			try {
				return Float.parseFloat(text);
			} catch (Exception e) {
				return Defaults.defaultValue(Float.TYPE);
			}
		});
		map.put(double.class, text -> {
			if (text == null || text.isEmpty()) {
				return Defaults.defaultValue(Double.TYPE);
			}
			try {
				return Double.parseDouble(text);
			} catch (Exception e) {
				return Defaults.defaultValue(Double.TYPE);
			}
		});

		map.put(Boolean.class, text -> {
			if (text == null || text.isEmpty()) {
				return null;
			}
			try {
				return Boolean.parseBoolean(text);
			} catch (Exception e) {
				return null;
			}
		});
		map.put(Byte.class, text -> {
			if (text == null || text.isEmpty()) {
				return null;
			}
			try {
				return Byte.parseByte(text);
			} catch (Exception e) {
				return null;
			}
		});
		map.put(Short.class, text -> {
			if (text == null || text.isEmpty()) {
				return null;
			}
			try {
				return Short.parseShort(text);
			} catch (Exception e) {
				return null;
			}
		});
		map.put(Integer.class, text -> {
			if (text == null || text.isEmpty()) {
				return null;
			}
			try {
				return Integer.parseInt(text);
			} catch (Exception e) {
				return null;
			}
		});
		map.put(Long.class, text -> {
			if (text == null || text.isEmpty()) {
				return null;
			}
			try {
				return Long.parseLong(text);
			} catch (Exception e) {
				return null;
			}
		});
		map.put(Float.class, text -> {
			if (text == null || text.isEmpty()) {
				return null;
			}
			try {
				return Float.parseFloat(text);
			} catch (Exception e) {
				return null;
			}
		});
		map.put(Double.class, text -> {
			if (text == null || text.isEmpty()) {
				return null;
			}
			try {
				return Double.parseDouble(text);
			} catch (Exception e) {
				return null;
			}
		});
		map.put(String.class, text -> text);
	}

	public static boolean containsType(Type type) {
		return map.containsKey(type);
	}

	public static Object convert(Type type, String text) {
		Function<String, Object> function = map.get(type);
		if (function != null) {
			return function.apply(text);
		}
		return JSONObject.parseObject(text, type);
	}
}

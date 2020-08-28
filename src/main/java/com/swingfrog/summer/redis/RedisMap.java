package com.swingfrog.summer.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisMap implements Map<String, String> {

	private final String sourceKey;
	
	RedisMap(String sourceKey) {
		this.sourceKey = sourceKey;
	}
	
	@Override
	public void clear() {
		RedisUtil.del(sourceKey);
	}

	@Override
	public boolean containsKey(Object key) {
		return RedisUtil.hexists(sourceKey, key.toString());
	}

	@Override
	public boolean containsValue(Object value) {
		List<String> values = RedisUtil.hvals(sourceKey);
		return values.contains(value);
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return RedisUtil.hgetAll(sourceKey).entrySet();
	}

	@Override
	public String get(Object key) {
		return RedisUtil.hget(sourceKey, key.toString());
	}

	@Override
	public boolean isEmpty() {
		return RedisUtil.hlen(sourceKey) > 0;
	}

	@Override
	public Set<String> keySet() {
		return RedisUtil.hkeys(sourceKey);
	}

	@Override
	public String put(String key, String value) {
		if (value != null) {
			RedisUtil.hset(sourceKey, key, value);
		} else {
			RedisUtil.hdel(sourceKey, key);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putAll(Map<? extends String, ? extends String> map) {
		if (map == null) {
			throw new NullPointerException();
		}
		if (map.size() == 0) {
			return;
		}
		RedisUtil.hmset(sourceKey, (Map<String, String>)map);
	}

	@Override
	public String remove(Object key) {
		RedisUtil.hdel(sourceKey, key.toString());
		return null;
	}

	@Override
	public int size() {
		return (int)RedisUtil.hlen(sourceKey);
	}

	@Override
	public Collection<String> values() {
		return RedisUtil.hvals(sourceKey);
	}

	@Override
	public String toString() {
		return RedisUtil.hgetAll(sourceKey).toString();
	}
}

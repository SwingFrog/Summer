package com.swingfrog.summer.redis;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class RedisSet implements Set<String> {

	private String sourceKey;
	
	RedisSet(String sourceKey) {
		this.sourceKey = sourceKey;
	}
	
	@Override
	public boolean add(String value) {
		if (value == null) {
			throw new NullPointerException();
		}
		return RedisUtil.sadd(sourceKey, value) > 0;
	}

	@Override
	public boolean addAll(Collection<? extends String> values) {
		if (values == null) {
			throw new NullPointerException();
		}
		if (values.size() == 0) {
			return false;
		}
		String[] strs = new String[values.size()];
		values.toArray(strs);
		return RedisUtil.sadd(sourceKey, strs) > 0;
	}

	@Override
	public void clear() {
		RedisUtil.del(sourceKey);
	}

	@Override
	public boolean contains(Object value) {
		return RedisUtil.smembers(sourceKey).contains(value);
	}

	@Override
	public boolean containsAll(Collection<?> values) {
		return RedisUtil.smembers(sourceKey).containsAll(values);
	}

	@Override
	public boolean isEmpty() {
		return RedisUtil.scard(sourceKey) > 0;
	}

	@Override
	public Iterator<String> iterator() {
		return RedisUtil.smembers(sourceKey).iterator();
	}

	@Override
	public boolean remove(Object value) {
		return RedisUtil.srem(sourceKey, value.toString());
	}

	@Override
	public boolean removeAll(Collection<?> values) {
		if (values == null) {
			throw new NullPointerException();
		}
		if (values.size() == 0) {
			return false;
		}
		String[] strs = new String[values.size()];
		values.toArray(strs);
		return RedisUtil.srem(sourceKey, strs) > 0;
	}

	@Override
	public boolean retainAll(Collection<?> values) {
		Set<String> set = RedisUtil.smembers(sourceKey);
		set.retainAll(values);
		String[] strs = new String[set.size()];
		set.toArray(strs);
		return RedisUtil.srem(sourceKey, strs) > 0;
	}

	@Override
	public int size() {
		return (int)RedisUtil.scard(sourceKey);
	}

	@Override
	public Object[] toArray() {
		return RedisUtil.smembers(sourceKey).toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return RedisUtil.smembers(sourceKey).toArray(arg0);
	}

	@Override
	public String toString() {
		return RedisUtil.smembers(sourceKey).toString();
	}
}

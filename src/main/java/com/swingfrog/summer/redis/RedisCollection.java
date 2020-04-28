package com.swingfrog.summer.redis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class RedisCollection implements Collection<String> {

	protected String sourceKey;
	
	RedisCollection(String sourceKey) {
		this.sourceKey = sourceKey;
	}
	
	@Override
	public boolean add(String value) {
		if (value == null) {
			throw new NullPointerException();
		}
		return RedisUtil.rpush(sourceKey, value);
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
		return RedisUtil.rpush(sourceKey, strs) > 0;
	}

	@Override
	public void clear() {
		RedisUtil.del(sourceKey);
	}

	@Override
	public boolean contains(Object value) {
		return RedisUtil.lrange(sourceKey, 0, -1).contains(value);
	}

	@Override
	public boolean containsAll(Collection<?> values) {
		return RedisUtil.lrange(sourceKey, 0, -1).containsAll(values);
	}

	@Override
	public boolean isEmpty() {
		return RedisUtil.llen(sourceKey) > 0;
	}

	@Override
	public Iterator<String> iterator() {
		return RedisUtil.lrange(sourceKey, 0, -1).iterator();
	}

	@Override
	public boolean remove(Object value) {
		return RedisUtil.lrem(sourceKey, 1, value.toString()) == 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> values) {
		boolean ok = true;
		List<String> temp = (List<String>)values;
		for (String value : temp) {
			if (RedisUtil.lrem(sourceKey, 1, value) < 1) {
				ok = false;
			}
		}
		return ok;
	}

	@Override
	public boolean retainAll(Collection<?> values) {
		boolean ok = true;
		List<String> list = RedisUtil.lrange(sourceKey, 0, -1);
		list.removeAll(values);
		for (String value : list) {
			if (RedisUtil.lrem(sourceKey, 1, value) < 1) {
				ok = false;
			}
		}
		return ok;
	}

	@Override
	public int size() {
		return (int)RedisUtil.llen(sourceKey);
	}

	@Override
	public Object[] toArray() {
		return RedisUtil.lrange(sourceKey, 0, -1).toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return RedisUtil.lrange(sourceKey, 0, -1).toArray(arg0);
	}

	@Override
	public String toString() {
		return RedisUtil.lrange(sourceKey, 0, -1).toString();
	}
	
}

package com.swingfrog.summer.redis;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class RedisList extends RedisCollection implements List<String> {

	RedisList(String sourceKey) {
		super(sourceKey);
	}

	@Override
	public void add(int index, String value) {
		if (value == null) {
			throw new NullPointerException();
		}
		List<String> list = RedisUtil.lrange(sourceKey, 0, -1);
		list.add(index, value);
		RedisUtil.del(sourceKey);
		String[] strs = new String[list.size()];
		list.toArray(strs);
		RedisUtil.rpush(sourceKey, strs);
	}

	@Override
	public boolean addAll(int index, Collection<? extends String> values) {
		if (values == null) {
			throw new NullPointerException();
		}
		if (values.size() == 0) {
			return false;
		}
		List<String> list = RedisUtil.lrange(sourceKey, 0, -1);
		list.addAll(index, values);
		RedisUtil.del(sourceKey);
		String[] strs = new String[list.size()];
		list.toArray(strs);
		return RedisUtil.rpush(sourceKey, strs) > 0;
	}

	@Override
	public String get(int index) {
		return RedisUtil.lindex(sourceKey, index);
	}

	@Override
	public int indexOf(Object value) {
		return RedisUtil.lrange(sourceKey, 0, -1).indexOf(value);
	}

	@Override
	public int lastIndexOf(Object value) {
		return RedisUtil.lrange(sourceKey, 0, -1).lastIndexOf(value);
	}

	@Override
	public ListIterator<String> listIterator() {
		return RedisUtil.lrange(sourceKey, 0, -1).listIterator();
	}

	@Override
	public ListIterator<String> listIterator(int index) {
		return RedisUtil.lrange(sourceKey, 0, -1).listIterator(index);
	}

	@Override
	public String remove(int index) {
		RedisUtil.ldel(sourceKey, index);
		return null;
	}

	@Override
	public String set(int index, String value) {
		if (value != null) {
			return RedisUtil.lset(sourceKey, index, value);
		} else {
			RedisUtil.ldel(sourceKey, index);
			return null;
		}
	}

	@Override
	public List<String> subList(int startIndex, int endIndex) {
		return RedisUtil.lrange(sourceKey, startIndex, endIndex);
	}

}

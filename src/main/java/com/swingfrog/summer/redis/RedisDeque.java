package com.swingfrog.summer.redis;

import com.google.common.collect.Lists;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class RedisDeque extends RedisCollection implements Deque<String> {
	
	RedisDeque(String sourceKey) {
		super(sourceKey);
	}

	@Override
	public void addFirst(String e) {
		if (e == null) {
			throw new NullPointerException();
		}
		if (!RedisUtil.lpush(sourceKey, e)) {
			throw new IllegalStateException();
		}
	}

	@Override
	public void addLast(String e) {
		if (e == null) {
			throw new NullPointerException();
		}
		if (!RedisUtil.rpush(sourceKey, e)) {
			throw new IllegalStateException();
		}
	}

	@Override
	public boolean offerFirst(String e) {
		if (e == null) {
			throw new NullPointerException();
		}
		return RedisUtil.lpush(sourceKey, e);
	}

	@Override
	public boolean offerLast(String e) {
		if (e == null) {
			throw new NullPointerException();
		}
		return RedisUtil.rpush(sourceKey, e);
	}

	@Override
	public String removeFirst() {
		String e = RedisUtil.lpop(sourceKey);
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e;
	}

	@Override
	public String removeLast() {
		String e = RedisUtil.rpop(sourceKey);
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e;
	}

	@Override
	public String pollFirst() {
		return RedisUtil.lpop(sourceKey);
	}

	@Override
	public String pollLast() {
		return RedisUtil.rpop(sourceKey);
	}

	@Override
	public String getFirst() {
		String e = RedisUtil.lindex(sourceKey, 0);
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e;
	}

	@Override
	public String getLast() {
		String e = RedisUtil.lindex(sourceKey, -1);
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e;
	}

	@Override
	public String peekFirst() {
		return RedisUtil.lindex(sourceKey, 0);
	}

	@Override
	public String peekLast() {
		return RedisUtil.lindex(sourceKey, -1);
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		return RedisUtil.lrem(sourceKey, 1, o.toString()) > 0;
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		RedisUtil.ldel(sourceKey, RedisUtil.lrange(sourceKey, 0, -1).lastIndexOf(o));
		return true;
	}

	@Override
	public boolean offer(String e) {
		if (e == null) {
			throw new NullPointerException();
		}
		return RedisUtil.rpush(sourceKey, e);
	}

	@Override
	public String remove() {
		String e = RedisUtil.lpop(sourceKey);
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e;
	}

	@Override
	public String poll() {
		return RedisUtil.lpop(sourceKey);
	}

	@Override
	public String element() {
		String e = RedisUtil.lindex(sourceKey, 0);
		if (e == null) {
			throw new NoSuchElementException();
		}
		return e;
	}

	@Override
	public String peek() {
		return RedisUtil.lindex(sourceKey, 0);
	}

	@Override
	public void push(String e) {
		if (e == null) {
			throw new NullPointerException();
		}
		if (!RedisUtil.rpush(sourceKey, e)) {
			throw new IllegalStateException();
		}
	}

	@Override
	public String pop() {
		return RedisUtil.lpop(sourceKey);
	}

	@Override
	public Iterator<String> descendingIterator() {
		List<String> list = RedisUtil.lrange(sourceKey, 0, -1);
		List<String> res = Lists.newArrayListWithCapacity(list.size());
		for (int i = list.size() - 1; i > 0; i--) {
			res.add(list.get(i));
		}
		return res.iterator();
	}

}

package com.swingfrog.summer.concurrent;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.redis.RedisSource;

public class SynchronizedMgr {

	private static final Logger log = LoggerFactory.getLogger(SynchronizedMgr.class);
	private RedisSource redisSource;
	
	private static class SingleCase {
		public static final SynchronizedMgr INSTANCE = new SynchronizedMgr();
	}
	
	private SynchronizedMgr() {
		redisSource = new RedisSource();
	}
	
	public static SynchronizedMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public boolean asyncLock(String key, String value) {
		if (key == null) {
			throw new NullPointerException("key is null");
		}
		if (value == null) {
			throw new NullPointerException("value is null");
		}
		if (redisSource.putAndSuccess(key, value)) {
			redisSource.setExpireTime(key, 12);
			return true;
		}
		return false;
	}
	
	public void lock(String key, String value) {
		while (!asyncLock(key, value)) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void unlock(String key, String value) {
		if (key == null) {
			throw new NullPointerException("key is null");
		}
		if (value == null) {
			throw new NullPointerException("value is null");
		}
		if (value.equals(redisSource.get(key))) {
			redisSource.remove(key);
		}
	}
	
	public void sync(String key, Runnable runnable) {
		key = String.join("-", "synchronized", key);
		String value = UUID.randomUUID().toString();
		try {
			lock(key, value);
			runnable.run();
		} finally {
			unlock(key, value);
		}
	}
}

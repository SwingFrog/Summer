package com.swingfrog.summer.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;

public class RedisUtil {

	public static RedisSource getRedisSource() {
		return new RedisSource();
	}
	
	/**清空数据*/
	public static void flushAll() {
		try {
			RedisMgr.get().getConnection().flushAll();
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**判断某个键是否存在*/
	public static boolean exists(String key) {
		try {
			return RedisMgr.get().getConnection().exists(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取所有key*/
	public static Set<String> keys() {
		try {
			return RedisMgr.get().getConnection().keys("*");
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取所有key*/
	public static Set<String> keys(String pattern) {
		try {
			return RedisMgr.get().getConnection().keys(pattern);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**设置键为key的过期时间为seconds秒*/
	public static boolean expire(String key, int seconds) {
		try {
			return RedisMgr.get().getConnection().expire(key, seconds) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取键为key数据项的剩余生存时间（秒）*/
	public static long ttl(String key) {
		try {
			return RedisMgr.get().getConnection().ttl(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**移除键为key属性项的生存时间限制*/
	public static boolean persist(String key) {
		try {
			return RedisMgr.get().getConnection().persist(key) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**查看键为key所对应value的数据类型*/
	public static String type(String key) {
		try {
			return RedisMgr.get().getConnection().type(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**新增键值对*/
	public static String set(String key, String value) {
		try {
			return RedisMgr.get().getConnection().set(key, value);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**如果key数据项已存在，则插入失败*/
	public static boolean setnx(String key, String value) {
		try {
			return RedisMgr.get().getConnection().setnx(key, value) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**增加数据项并设置有效时间*/
	public static String setex(String key, int seconds, String value) {
		try {
			return RedisMgr.get().getConnection().setex(key, seconds, value);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**删除键为key的数据项*/
	public static boolean del(String key) {
		try {
			return RedisMgr.get().getConnection().del(key) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取键为key对应的value*/
	public static String get(String key) {
		try {
			return RedisMgr.get().getConnection().get(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**增加多个键值对*/
	public static String mset(String ...keyAndValue) {
		try {
			return RedisMgr.get().getConnection().mset(keyAndValue);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取多个key对应value*/
	public static List<String> mget(String ...key) {
		try {
			return RedisMgr.get().getConnection().mget(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**删除多个key对应数据项*/
	public static long del(String ...key) {
		try {
			return RedisMgr.get().getConnection().del(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取key对应value并更新value*/
	public static String getSet(String key, String value) {
		try {
			return RedisMgr.get().getConnection().getSet(key, value);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**将key对应的value自加1*/
	public static boolean incr(String key) {
		try {
			return RedisMgr.get().getConnection().incr(key) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**将key对应的value自加n*/
	public static boolean incrBy(String key, int n) {
		try {
			return RedisMgr.get().getConnection().incrBy(key, n) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**将key对应的value自加n*/
	public static boolean incrByFloat(String key, float n) {
		try {
			return RedisMgr.get().getConnection().incrByFloat(key, n) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**将key对应的value自减1*/
	public static boolean decr(String key) {
		try {
			return RedisMgr.get().getConnection().decr(key) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**将key对应的value自减n*/
	public static boolean decrBy(String key, int n) {
		try {
			return RedisMgr.get().getConnection().decrBy(key, n) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**
	 * map
	 */
	
	/**添加一个hash*/
	public static String hmset(String key, Map<String, String> map) {
		try {
			return RedisMgr.get().getConnection().hmset(key, map);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**往hash插入一个元素（k-v）*/
	public static boolean hset(String key, String hkey, String hvalue) {
		try {
			return RedisMgr.get().getConnection().hset(key, hkey, hvalue) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取hash所有（k-v）元素*/
	public static Map<String, String> hgetAll(String key) {
		try {
			return RedisMgr.get().getConnection().hgetAll(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取hash所有元素的key*/
	public static Set<String> hkeys(String key) {
		try {
			return RedisMgr.get().getConnection().hkeys(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取hash所有元素的value*/
	public static List<String> hvals(String key) {
		try {
			return RedisMgr.get().getConnection().hvals(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**把hash中key对应元素val+=n*/
	public static boolean hincrBy(String key, String hkey, int n) {
		try {
			return RedisMgr.get().getConnection().hincrBy(key, hkey, n) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**把hash中key对应元素val+=n*/
	public static boolean hincrByFloat(String key, String hkey, float n) {
		try {
			return RedisMgr.get().getConnection().hincrByFloat(key, hkey, n) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**从hash删除一个元素*/
	public static boolean hdel(String key, String hkey) {
		try {
			return RedisMgr.get().getConnection().hdel(key, hkey) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**从hash删除多个元素*/
	public static long hdel(String key, String ...hkey) {
		try {
			return RedisMgr.get().getConnection().hdel(key, hkey);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取hash中元素个数*/
	public static long hlen(String key) {
		try {
			return RedisMgr.get().getConnection().hlen(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**判断hash是否存在hkey对应元素*/
	public static boolean hexists(String key, String hkey) {
		try {
			return RedisMgr.get().getConnection().hexists(key, hkey);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取hash中多个元素value*/
	public static List<String> hmget(String key, String ...hkey) {
		try {
			return RedisMgr.get().getConnection().hmget(key, hkey);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取hash中一个元素value*/
	public static String hget(String key, String hkey) {
		try {
			return RedisMgr.get().getConnection().hget(key, hkey);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**添加一个list*/
	public static boolean lpush(String key, String ...val) {
		try {
			return RedisMgr.get().getConnection().lpush(key, val) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**往key对应list左插入一个元素val*/
	public static boolean lpush(String key, String val) {
		try {
			return RedisMgr.get().getConnection().lpush(key, val) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取key对应list期间[i,j]的元素*/
	public static List<String> lrange(String key, int startIndex, int endIndex) {
		try {
			return RedisMgr.get().getConnection().lrange(key, startIndex, endIndex);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**删除指定元素val个数num*/
	public static long lrem(String key, int num, String val) {
		try {
			return RedisMgr.get().getConnection().lrem(key, num, val);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**删除list区间[i,j]之外的元素*/
	public static String ltrim(String key, int startIndex, int endIndex) {
		try {
			return RedisMgr.get().getConnection().ltrim(key, startIndex, endIndex);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**删除index的元素*/
	public static void ldel(String key, int index) {
		try {
			Transaction multi = RedisMgr.get().getConnection().multi();
			multi.lset(key, index, "__deleted__");
			multi.lrem(key, 1, "__deleted__");
			multi.exec();
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**key对应list左出栈一个元素*/
	public static String lpop(String key) {
		try {
			return RedisMgr.get().getConnection().lpop(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**key对应list右插入一个元素val*/
	public static boolean rpush(String key, String val) {
		try {
			return RedisMgr.get().getConnection().rpush(key, val) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**key对应list右插入多个元素val*/
	public static long rpush(String key, String ...val) {
		try {
			return RedisMgr.get().getConnection().rpush(key, val);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**key对应list右出栈一个元素*/
	public static String rpop(String key) {
		try {
			return RedisMgr.get().getConnection().rpop(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**修改key对应list指定下标index的元素*/
	public static String lset(String key, int index, String val) {
		try {
			return RedisMgr.get().getConnection().lset(key, index, val);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取key对应list的长度*/
	public static long llen(String key) {
		try {
			return RedisMgr.get().getConnection().llen(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取key对应list下标为index的元素*/
	public static String lindex(String key, int index) {
		try {
			return RedisMgr.get().getConnection().lindex(key, index);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**添加多个val*/
	public static long sadd(String key, String ...val) {
		try {
			return RedisMgr.get().getConnection().sadd(key, val);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取key对应set的所有元素*/
	public static Set<String> smembers(String key) {
		try {
			return RedisMgr.get().getConnection().smembers(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**删除一个值为val的元素*/
	public static boolean srem(String key, String val) {
		try {
			return RedisMgr.get().getConnection().srem(key, val) > 0;
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**删除值为val1,val2的元素*/
	public static long srem(String key, String ...val) {
		try {
			return RedisMgr.get().getConnection().srem(key, val);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**随机出栈set里的一个元素*/
	public static String spop(String key) {
		try {
			return RedisMgr.get().getConnection().spop(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**获取set中元素个数*/
	public static long scard(String key) {
		try {
			return RedisMgr.get().getConnection().scard(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**排序*/
	public static List<String> sort(String key) {
		try {
			return RedisMgr.get().getConnection().sort(key);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
	
	/**排序*/
	public static List<String> sort(String key, SortingParams sortingParameters) {
		try {
			return RedisMgr.get().getConnection().sort(key, sortingParameters);
		} finally {
			RedisMgr.get().discardConnectionFromRedis();
		}
	}
}

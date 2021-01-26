package com.swingfrog.summer.redis;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisMgr {

	private static final Logger log = LoggerFactory.getLogger(RedisMgr.class);

	public static final String DEFAULT_CONFIG_PATH = "config/redis.properties";

	private JedisPool jedisPool;
	private final ThreadLocal<ConnInfo> local = ThreadLocal.withInitial(ConnInfo::new);
	
	private static class SingleCase {
		public static final RedisMgr INSTANCE = new RedisMgr();
	}
	
	private RedisMgr() {
		
	}
	
	public static RedisMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void loadConfig(String path) throws Exception {
		File file = new File(path);
		if (DEFAULT_CONFIG_PATH.equals(path)) {
			if (file.exists()) {
				loadConfig(new FileInputStream(file));
			} else {
				log.debug("used default redis config.");
				loadDefaultConfig();
			}
		} else {
			loadConfig(new FileInputStream(file));
		}
	}
	
	public void loadConfig(InputStream in) throws Exception {
		Properties pro = new Properties();
		pro.load(in);
		RedisConfig redisConfig = new RedisConfig();
		redisConfig.setUrl(pro.getProperty("url"));
		redisConfig.setPort(Integer.parseInt(pro.getProperty("port")));
		redisConfig.setTimeout(Integer.parseInt(pro.getProperty("timeout")));
		redisConfig.setPassword(pro.getProperty("password"));
		redisConfig.setBlockWhenExhausted(Boolean.parseBoolean(pro.getProperty("blockWhenExhausted")));
		redisConfig.setEvictionPolicyClassName(pro.getProperty("evictionPolicyClassName"));
		redisConfig.setJmxEnabled(Boolean.parseBoolean(pro.getProperty("jmxEnabled")));
		redisConfig.setMaxIdle(Integer.parseInt(pro.getProperty("maxIdle")));
		redisConfig.setMaxTotal(Integer.parseInt(pro.getProperty("maxTotal")));
		redisConfig.setMaxWaitMillis(Long.parseLong(pro.getProperty("maxWaitMillis")));
		redisConfig.setTestOnBorrow(Boolean.parseBoolean(pro.getProperty("testOnBorrow")));
		in.close();
		pro.clear();
		loadJedisPool(redisConfig);
	}

	private void loadDefaultConfig() {
		RedisConfig redisConfig = new RedisConfig();
		redisConfig.setUrl("127.0.0.1");
		redisConfig.setPort(6379);
		redisConfig.setTimeout(3000);
		redisConfig.setPassword("123456");
		redisConfig.setBlockWhenExhausted(true);
		redisConfig.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
		redisConfig.setJmxEnabled(true);
		redisConfig.setMaxIdle(8);
		redisConfig.setMaxTotal(200);
		redisConfig.setMaxWaitMillis(100000);
		redisConfig.setTestOnBorrow(true);
		loadJedisPool(redisConfig);
	}

	private void loadJedisPool(RedisConfig redisConfig) {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setBlockWhenExhausted(redisConfig.isBlockWhenExhausted());
		jedisPoolConfig.setEvictionPolicyClassName(redisConfig.getEvictionPolicyClassName());
		jedisPoolConfig.setJmxEnabled(redisConfig.isJmxEnabled());
		jedisPoolConfig.setMaxIdle(redisConfig.getMaxIdle());
		jedisPoolConfig.setMaxTotal(redisConfig.getMaxTotal());
		jedisPoolConfig.setMaxWaitMillis(redisConfig.getMaxWaitMillis());
		jedisPoolConfig.setTestOnBorrow(redisConfig.isTestOnBorrow());
		jedisPool = new JedisPool(jedisPoolConfig, redisConfig.getUrl(), redisConfig.getPort(), redisConfig.getTimeout(), redisConfig.getPassword());
	}
	
	public Jedis getConnection() {
		Jedis conn = local.get().getConn();
		if (conn == null) {
			conn = jedisPool.getResource();
			local.get().setConn(conn);
		}
		return conn;
	}
	
	private void discardConnection() {
		Jedis conn = local.get().getConn();
		if (conn != null) {
			conn.close();
		}
		local.get().dispose();
	}
	
	public void discardConnectionFromRedis() {
		if (!local.get().isRemoteDiscard() && !local.get().isServiceDiscard()) {
			discardConnection();
		}
	}
	
	public void discardConnectionFromService() {
		if (!local.get().isRemoteDiscard()) {
			discardConnection();
		}
	}
	
	public void discardConnectionFromRemote() {
		discardConnection();
	}
	
	public void setDiscardConnectionLevelForService() {
		local.get().setServiceDiscard(true);
	}
	
	public void setDiscardConnectionLevelForRemote() {
		local.get().setRemoteDiscard(true);
	}
	
	private static class ConnInfo {
		private boolean serviceDiscard;
		private boolean remoteDiscard;
		private Jedis conn;
		public ConnInfo() {
			dispose();
		}
		public boolean isServiceDiscard() {
			return serviceDiscard;
		}
		public void setServiceDiscard(boolean serviceDiscard) {
			this.serviceDiscard = serviceDiscard;
		}
		public boolean isRemoteDiscard() {
			return remoteDiscard;
		}
		public void setRemoteDiscard(boolean remoteDiscard) {
			this.remoteDiscard = remoteDiscard;
		}
		public Jedis getConn() {
			return conn;
		}
		public void setConn(Jedis conn) {
			this.conn = conn;
		}
		public void dispose() {
			serviceDiscard = false;
			remoteDiscard = false;
			conn = null;
		}
	}
	
	private static class RedisConfig {
		private String url;
		private int port;
		private int timeout;
		private String password;
		private boolean blockWhenExhausted;
		private String evictionPolicyClassName;
		private boolean jmxEnabled;
		private int maxIdle;
		private int maxTotal;
		private long maxWaitMillis;
		private boolean testOnBorrow;
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public int getTimeout() {
			return timeout;
		}
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public boolean isBlockWhenExhausted() {
			return blockWhenExhausted;
		}
		public void setBlockWhenExhausted(boolean blockWhenExhausted) {
			this.blockWhenExhausted = blockWhenExhausted;
		}
		public String getEvictionPolicyClassName() {
			return evictionPolicyClassName;
		}
		public void setEvictionPolicyClassName(String evictionPolicyClassName) {
			this.evictionPolicyClassName = evictionPolicyClassName;
		}
		public boolean isJmxEnabled() {
			return jmxEnabled;
		}
		public void setJmxEnabled(boolean jmxEnabled) {
			this.jmxEnabled = jmxEnabled;
		}
		public int getMaxIdle() {
			return maxIdle;
		}
		public void setMaxIdle(int maxIdle) {
			this.maxIdle = maxIdle;
		}
		public int getMaxTotal() {
			return maxTotal;
		}
		public void setMaxTotal(int maxTotal) {
			this.maxTotal = maxTotal;
		}
		public long getMaxWaitMillis() {
			return maxWaitMillis;
		}
		public void setMaxWaitMillis(long maxWaitMillis) {
			this.maxWaitMillis = maxWaitMillis;
		}
		public boolean isTestOnBorrow() {
			return testOnBorrow;
		}
		public void setTestOnBorrow(boolean testOnBorrow) {
			this.testOnBorrow = testOnBorrow;
		}
	}
	
}

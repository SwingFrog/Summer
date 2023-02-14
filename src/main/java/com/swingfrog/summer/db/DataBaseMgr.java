package com.swingfrog.summer.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBaseMgr {

	private static final Logger log = LoggerFactory.getLogger(DataBaseMgr.class);

	public static final String DEFAULT_CONFIG_PATH = "config/db.properties";

	private HikariDataSource dataSource;
	private final Map<String, HikariDataSource> otherDataSourceMap = Maps.newHashMap();
	private final ThreadLocal<ConnInfo> local = ThreadLocal.withInitial(ConnInfo::new);
	
	private static class SingleCase {
		public static final DataBaseMgr INSTANCE = new DataBaseMgr();
	}
	
	private DataBaseMgr() {
		
	}
	
	public static DataBaseMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void loadConfig(String path) throws Exception {
		if (DEFAULT_CONFIG_PATH.equals(path)) {
			File file = new File(path);
			if (file.exists()) {
				dataSource = getDataSource(new FileInputStream(file));
			} else {
				log.debug("used default db config.");
				dataSource = getDefaultDataSource();
			}
		} else {
			dataSource = getDataSource(new FileInputStream(path));
		}
	}
	
	public HikariDataSource getDataSource(InputStream in) throws Exception {
		Properties properties = new Properties();
		properties.load(in);
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(properties.getProperty("driverClassName"));
		config.setJdbcUrl(properties.getProperty("jdbcUrl"));
		config.setUsername(properties.getProperty("username"));
		config.setPassword(properties.getProperty("password"));
		config.setPoolName(properties.getProperty("poolName"));
		config.setMinimumIdle(Integer.parseInt(properties.getProperty("minimumIdle")));
		config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("maximumPoolSize")));
		config.setConnectionTimeout(Long.parseLong(properties.getProperty("connectionTimeout")));
		config.setConnectionTestQuery(properties.getProperty("connectionTestQuery"));
		in.close();
		properties.clear();
		return new HikariDataSource(config);
	}

	private HikariDataSource getDefaultDataSource() {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
		config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&useSSL=false");
		config.setUsername("root");
		config.setPassword("123456");
		config.setPoolName("Test");
		config.setMinimumIdle(1);
		config.setMaximumPoolSize(10);
		config.setConnectionTimeout(30_000);
		config.setConnectionTestQuery("SELECT 1");
		return new HikariDataSource(config);
	}

	public void loadConfigForOther(String topic, String path) throws Exception {
		loadConfigForOther(topic, new FileInputStream(path));
	}

	public void loadConfigForOther(String topic, InputStream in) throws Exception {
		Objects.requireNonNull(topic, "topic not null");
		otherDataSourceMap.put(topic, getDataSource(in));
	}
	
	public Connection getConnection() throws SQLException {
		Connection conn = local.get().getConn();
		if (conn == null) {
			conn = dataSource.getConnection();
			if (local.get().isTransaction()) {
				conn.setAutoCommit(false);
			}
			local.get().setConn(conn);
		}
		return conn;
	}

	public Connection getConnection(String topic) throws SQLException {
		if (!otherDataSourceMap.containsKey(topic)) {
			throw new DaoRuntimeException(String.format("not found data source for the topic[%s]", topic));
		}
		Connection conn = local.get().getOtherConn(topic);
		if (conn == null) {
			conn = otherDataSourceMap.get(topic).getConnection();
			if (local.get().isTransaction()) {
				conn.setAutoCommit(false);
			}
			Connection old = local.get().putOtherConn(topic, conn);
			if (old != null) {
				return old;
			}
		}
		return conn;
	}
	
	private void discardConnection() throws SQLException {
		Connection conn = local.get().getConn();
		if (conn != null) {
			conn.setAutoCommit(true);
			conn.close();
		}
		for (Connection otherConn : local.get().listOtherConn()) {
			otherConn.setAutoCommit(true);
			otherConn.close();
		}
		local.get().dispose();
	}
	
	public void discardConnection(Object owner) throws SQLException {
		Object oldOwner = local.get().getOwner();
		if (oldOwner == null || oldOwner == owner) {
			discardConnection();
		}
	}

	public void openTransaction() {
		local.get().setTransaction(true);
	}

	public boolean notOpenTransaction() {
		return !local.get().isTransaction();
	}
	
	public void setOwner(Object owner) {
		local.get().setOwner(owner);
	}

	private static class ConnInfo {
		private Connection conn;
		private final ConcurrentMap<String, Connection> otherConnMap = Maps.newConcurrentMap();
		private Object owner;
		private boolean transaction;
		public ConnInfo() {
			dispose();
		}
		public Connection getConn() {
			return conn;
		}
		public void setConn(Connection conn) {
			this.conn = conn;
		}
		public Object getOwner() {
			return owner;
		}
		public void setOwner(Object owner) {
			this.owner = owner;
		}
		public boolean isTransaction() {
			return transaction;
		}
		public void setTransaction(boolean transaction) {
			this.transaction = transaction;
		}
		public Connection getOtherConn(String topic) {
			return otherConnMap.get(topic);
		}
		public Connection putOtherConn(String topic, Connection conn) {
			return otherConnMap.putIfAbsent(topic, conn);
		}
		public Collection<Connection> listOtherConn() {
			return otherConnMap.values();
		}
		public void dispose() {
			conn = null;
			if (!otherConnMap.isEmpty()) {
				otherConnMap.clear();
			}
			owner = null;
			transaction = false;
		}
	}
}

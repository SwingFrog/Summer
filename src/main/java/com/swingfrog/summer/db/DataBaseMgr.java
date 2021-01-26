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

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBaseMgr {

	private static final Logger log = LoggerFactory.getLogger(DataBaseMgr.class);

	public static final String DEFAULT_CONFIG_PATH = "config/db.properties";

	private DruidDataSource dataSource;
	private final Map<String, DruidDataSource> otherDataSourceMap = Maps.newHashMap();
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
				loadConfig(new FileInputStream(file));
			} else {
				log.debug("used default db config.");
				dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(createDefaultProperties());
			}
		} else {
			loadConfig(new FileInputStream(path));
		}
	}
	
	public void loadConfig(InputStream in) throws Exception {
		Properties properties = new Properties();
		properties.load(in);
		dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
	}

	private Properties createDefaultProperties() {
		Properties properties = new Properties();
		properties.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
		properties.setProperty("url", "jdbc:mysql://127.0.0.1:3306/db_test?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai");
		properties.setProperty("username", "root");
		properties.setProperty("password", "123456");
		properties.setProperty("filters", "stat");
		properties.setProperty("initialSize", "2");
		properties.setProperty("maxActive", "300");
		properties.setProperty("maxWait", "60000");
		properties.setProperty("timeBetweenEvictionRunsMillis", "60000");
		properties.setProperty("minEvictableIdleTimeMillis", "300000");
		properties.setProperty("validationQuery", "SELECT 1");
		properties.setProperty("testWhileIdle", "true");
		properties.setProperty("testOnBorrow", "false");
		properties.setProperty("testOnReturn", "false");
		properties.setProperty("poolPreparedStatements", "false");
		properties.setProperty("maxPoolPreparedStatementPerConnectionSize", "200");
		return properties;
	}

	public void loadConfigForOther(String topic, String path) throws Exception {
		loadConfigForOther(topic, new FileInputStream(path));
	}

	public void loadConfigForOther(String topic, InputStream in) throws Exception {
		Objects.requireNonNull(topic, "topic not null");
		Properties properties = new Properties();
		properties.load(in);
		otherDataSourceMap.put(topic, (DruidDataSource) DruidDataSourceFactory.createDataSource(properties));
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
	
	public void discardConnectionFromDao() throws SQLException {
		if (!local.get().isRemoteDiscard() && !local.get().isServiceDiscard()) {
			discardConnection();
		}
	}
	
	public void discardConnectionFromService() throws SQLException {
		if (!local.get().isRemoteDiscard()) {
			discardConnection();
		}
	}
	
	public void discardConnectionFromRemote() throws SQLException {
		discardConnection();
	}

	public void openTransaction() {
		local.get().setTransaction(true);
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
		private Connection conn;
		private final ConcurrentMap<String, Connection> otherConnMap = Maps.newConcurrentMap();
		private boolean transaction;
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
		public Connection getConn() {
			return conn;
		}
		public void setConn(Connection conn) {
			this.conn = conn;
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
			serviceDiscard = false;
			remoteDiscard = false;
			conn = null;
			otherConnMap.clear();
			transaction = false;
		}
	}
}

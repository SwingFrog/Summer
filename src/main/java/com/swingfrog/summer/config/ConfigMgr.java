package com.swingfrog.summer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigMgr {

	private static final Logger log = LoggerFactory.getLogger(ConfigMgr.class);

	public static final String DEFAULT_CONFIG_PATH = "config/server.properties";

	private final ServerConfig serverConfig;
	private ServerConfig[] minorConfigs;
	private ClientGroupConfig clientGroupConfig;
	private ClientConfig[] clientConfigs;

	private static class SingleCase {
		public static final ConfigMgr INSTANCE = new ConfigMgr();
	}
	
	private ConfigMgr() {
		serverConfig = new ServerConfig();
	}
	
	public static ConfigMgr get() {
		return SingleCase.INSTANCE;
	}

	public void loadConfig(String path) throws IOException, IntrospectionException {
		if (DEFAULT_CONFIG_PATH.equals(path)) {
			File file = new File(path);
			if (file.exists()) {
				loadConfig(new FileInputStream(file));
			} else {
				log.debug("used default server config.");
				loadDefaultConfig();
			}
		} else {
			loadConfig(new FileInputStream(path));
		}
	}

	public void loadConfig(InputStream in) throws IOException, IntrospectionException {
		Properties pro = new Properties();
		pro.load(in);
		ConfigUtil.loadDataWithBean(pro, "server.", serverConfig);
		String minorList = pro.getProperty("server.minorList");
		if (minorList != null && minorList.length() > 0) {
			String[] minors = ConfigUtil.getValueByTypeAndString(String[].class, minorList);
			minorConfigs = new ServerConfig[minors.length];
			for (int i = 0; i < minorConfigs.length; i ++) {
				minorConfigs[i] = new ServerConfig();
				ConfigUtil.loadDataWithBean(pro, String.format("minor.%s.", minors[i]), minorConfigs[i]);
			}
		}
		String clientList = pro.getProperty("server.clientList");
		if (clientList != null && clientList.length() > 0) {
			clientGroupConfig = new ClientGroupConfig();
			ConfigUtil.loadDataWithBean(pro, "server.clientGroup.", clientGroupConfig);
			String[] clients = ConfigUtil.getValueByTypeAndString(String[].class, clientList);
			clientConfigs = new ClientConfig[clients.length];
			for (int i = 0; i < clientConfigs.length; i ++) {
				clientConfigs[i] = new ClientConfig();
				ConfigUtil.loadDataWithBean(pro, String.format("client.%s.", clients[i]), clientConfigs[i]);
			}
		}
		in.close();
		pro.clear();
	}

	private void loadDefaultConfig() {
		serverConfig.setCluster("Http");
		serverConfig.setServerName("http");
		serverConfig.setAddress("127.0.0.1");
		serverConfig.setPort(8080);
		serverConfig.setProtocol("Http");
		serverConfig.setCharset("UTF-8");
		serverConfig.setBossThread(1);
		serverConfig.setWorkerThread(1);
		serverConfig.setEventThread(1);
		serverConfig.setMsgLength(104857600);
		serverConfig.setHeartSec(40);
		serverConfig.setColdDownMs(10);
		serverConfig.setOptionSoBacklog(1024);
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public ServerConfig[] getMinorConfigs() {
		return minorConfigs;
	}

	public ClientGroupConfig getClientGroupConfig() {
		return clientGroupConfig;
	}

	public ClientConfig[] getClientConfigs() {
		return clientConfigs;
	}
	
}

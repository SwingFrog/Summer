package com.swingfrog.summer.config;

import java.beans.IntrospectionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigMgr {

	private ServerConfig serverConfig;
	private ServerConfig[] minorConfigs;
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
		loadConfig(new FileInputStream(path));
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

	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public ServerConfig[] getMinorConfigs() {
		return minorConfigs;
	}
	
	public ClientConfig[] getClientConfigs() {
		return clientConfigs;
	}
	
}

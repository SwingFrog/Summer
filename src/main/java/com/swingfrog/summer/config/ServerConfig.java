package com.swingfrog.summer.config;

import java.util.Arrays;

public class ServerConfig {

	/**集群*/
	private String cluster;
	/**名称*/
	private String serverName;
	/**地址*/
	private String address;
	/**端口*/
	private int port;
	/**协议*/
	private String protocol;
	/**编码*/
	private String charset;
	/**秘钥*/
	private String password;
	/**监听线程数*/
	private int bossThread;
	/**读写线程数*/
	private int workerThread;
	/**业务线程数*/
	private int eventThread;
	/**消息长度*/
	private int msgLength;
	/**心跳时间*/
	private int heartSec;
	/**每个客户端冷却时间*/
	private int coldDownMs;
	/**开启限制地址*/
	private boolean allowAddressEnable;
	/**允许客户端地址*/
	private String[] allowAddressList;
	/**使用主端口的线程池 (监听线程池, 读写线程池, 业务线程池)*/
	private boolean useMainServerThreadPool;
	/**SOCKET: SO_BACKLOG*/
	private int optionSoBacklog;
	
	public String getCluster() {
		return cluster;
	}
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getBossThread() {
		return bossThread;
	}
	public void setBossThread(int bossThread) {
		this.bossThread = bossThread;
	}
	public int getWorkerThread() {
		return workerThread;
	}
	public void setWorkerThread(int workerThread) {
		this.workerThread = workerThread;
	}
	public int getEventThread() {
		return eventThread;
	}
	public void setEventThread(int eventThread) {
		this.eventThread = eventThread;
	}
	public int getMsgLength() {
		return msgLength;
	}
	public void setMsgLength(int msgLength) {
		this.msgLength = msgLength;
	}
	public int getHeartSec() {
		return heartSec;
	}
	public void setHeartSec(int heartSec) {
		this.heartSec = heartSec;
	}
	public int getColdDownMs() {
		return coldDownMs;
	}
	public void setColdDownMs(int coldDownMs) {
		this.coldDownMs = coldDownMs;
	}
	public boolean isAllowAddressEnable() {
		return allowAddressEnable;
	}
	public void setAllowAddressEnable(boolean allowAddressEnable) {
		this.allowAddressEnable = allowAddressEnable;
	}
	public String[] getAllowAddressList() {
		return allowAddressList;
	}
	public void setAllowAddressList(String[] allowAddressList) {
		this.allowAddressList = allowAddressList;
	}
	public boolean isUseMainServerThreadPool() {
		return useMainServerThreadPool;
	}
	public void setUseMainServerThreadPool(boolean useMainServerThreadPool) {
		this.useMainServerThreadPool = useMainServerThreadPool;
	}
	public int getOptionSoBacklog() {
		return optionSoBacklog;
	}
	public void setOptionSoBacklog(int optionSoBacklog) {
		this.optionSoBacklog = optionSoBacklog;
	}

	@Override
	public String toString() {
		return "ServerConfig{" +
				"cluster='" + cluster + '\'' +
				", serverName='" + serverName + '\'' +
				", address='" + address + '\'' +
				", port=" + port +
				", protocol='" + protocol + '\'' +
				", charset='" + charset + '\'' +
				", password='" + password + '\'' +
				", bossThread=" + bossThread +
				", workerThread=" + workerThread +
				", eventThread=" + eventThread +
				", msgLength=" + msgLength +
				", heartSec=" + heartSec +
				", coldDownMs=" + coldDownMs +
				", allowAddressEnable=" + allowAddressEnable +
				", allowAddressList=" + Arrays.toString(allowAddressList) +
				", useMainServerThreadPool=" + useMainServerThreadPool +
				", optionSoBacklog=" + optionSoBacklog +
				'}';
	}

}

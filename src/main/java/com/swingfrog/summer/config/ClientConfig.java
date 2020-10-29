package com.swingfrog.summer.config;

public class ClientConfig {
	
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
	/**消息长度*/
	private int msgLength;
	/**心跳时间*/
	private int heartSec;
	/**重连等待时间*/
	private int reconnectMs;
	/**超时时间*/
	private int syncRemoteTimeOutMs;
	/**连接数量*/
	private int connectNum;
	
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
	public int getReconnectMs() {
		return reconnectMs;
	}
	public void setReconnectMs(int reconnectMs) {
		this.reconnectMs = reconnectMs;
	}
	public int getSyncRemoteTimeOutMs() {
		return syncRemoteTimeOutMs;
	}
	public void setSyncRemoteTimeOutMs(int syncRemoteTimeOutMs) {
		this.syncRemoteTimeOutMs = syncRemoteTimeOutMs;
	}
	public int getConnectNum() {
		return connectNum;
	}
	public void setConnectNum(int connectNum) {
		this.connectNum = connectNum;
	}
	@Override
	public String toString() {
		return "ClientConfig{" +
				"cluster='" + cluster + '\'' +
				", serverName='" + serverName + '\'' +
				", address='" + address + '\'' +
				", port=" + port +
				", protocol='" + protocol + '\'' +
				", charset='" + charset + '\'' +
				", password='" + password + '\'' +
				", msgLength=" + msgLength +
				", heartSec=" + heartSec +
				", reconnectMs=" + reconnectMs +
				", syncRemoteTimeOutMs=" + syncRemoteTimeOutMs +
				", connectNum=" + connectNum +
				'}';
	}
}

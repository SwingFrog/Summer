package com.swingfrog.summer.server;

import com.swingfrog.summer.config.ServerConfig;
import com.swingfrog.summer.protocol.ProtocolConst;

import java.util.concurrent.ExecutorService;

public class ServerContext {

	private final ServerConfig config;
	private final SessionHandlerGroup sessionHandlerGroup;
	private final SessionContextGroup sessionContextGroup;
	private final ExecutorService eventExecutor;
	private final ExecutorService pushExecutor;
	private final boolean http;
	private final boolean protobuf;
	
	public ServerContext(ServerConfig config,
						 SessionHandlerGroup sessionHandlerGroup,
						 SessionContextGroup sessionContextGroup,
						 ExecutorService eventExecutor,
						 ExecutorService pushExecutor) {
		this.config = config;
		this.sessionHandlerGroup = sessionHandlerGroup;
		this.sessionContextGroup = sessionContextGroup;
		this.eventExecutor = eventExecutor;
		this.pushExecutor = pushExecutor;
		String protocol = config.getProtocol();
		http = ProtocolConst.isHttp(protocol);
		protobuf = ProtocolConst.isProtobuf(protocol);
	}
	public ServerConfig getConfig() {
		return config;
	}
	public SessionHandlerGroup getSessionHandlerGroup() {
		return sessionHandlerGroup;
	}
	public SessionContextGroup getSessionContextGroup() {
		return sessionContextGroup;
	}
	public ExecutorService getEventExecutor() {
		return eventExecutor;
	}
	public ExecutorService getPushExecutor() {
		return pushExecutor;
	}
	public boolean isHttp() {
		return http;
	}
	public boolean isProtobuf() {
		return protobuf;
	}
}

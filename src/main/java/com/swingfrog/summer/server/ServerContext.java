package com.swingfrog.summer.server;

import com.swingfrog.summer.config.ServerConfig;

import java.util.concurrent.ExecutorService;

public class ServerContext {

	private ServerConfig config;
	private SessionHandlerGroup sessionHandlerGroup;
	private SessionContextGroup sessionContextGroup;
	private ExecutorService eventExecutor;
	private final ExecutorService pushExecutor;
	
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
	}
	public ServerConfig getConfig() {
		return config;
	}
	public void setConfig(ServerConfig config) {
		this.config = config;
	}
	public SessionHandlerGroup getSessionHandlerGroup() {
		return sessionHandlerGroup;
	}
	public void setSessionHandlerGroup(SessionHandlerGroup sessionHandlerGroup) {
		this.sessionHandlerGroup = sessionHandlerGroup;
	}
	public SessionContextGroup getSessionContextGroup() {
		return sessionContextGroup;
	}
	public void setSessionContextGroup(SessionContextGroup sessionContextGroup) {
		this.sessionContextGroup = sessionContextGroup;
	}
	public ExecutorService getEventExecutor() {
		return eventExecutor;
	}
	public void setEventExecutor(ExecutorService eventExecutor) {
		this.eventExecutor = eventExecutor;
	}
	public ExecutorService getPushExecutor() {
		return pushExecutor;
	}
}

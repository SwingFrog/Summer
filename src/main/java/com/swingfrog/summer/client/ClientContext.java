package com.swingfrog.summer.client;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.protocol.SessionRequest;

import io.netty.channel.ChannelHandlerContext;

public class ClientContext {

	private final ClientConfig config;
	private final Client client;
	private final AtomicReference<ChannelHandlerContext> channel = new AtomicReference<>();
	private final ExecutorService eventExecutor;
	private final ExecutorService pushExecutor;
	private volatile long lastRecvTime;
	private final ConcurrentLinkedQueue<SessionRequest> requestQueue = new ConcurrentLinkedQueue<>();
	
	public ClientContext(ClientConfig config, Client client, ExecutorService eventExecutor, ExecutorService pushExecutor) {
		this.config = config;
		this.client = client;
		this.eventExecutor = eventExecutor;
		this.pushExecutor = pushExecutor;
		lastRecvTime = System.currentTimeMillis();
	}
	public ClientConfig getConfig() {
		return config;
	}
	public Client getClient() {
		return client;
	}
	public ChannelHandlerContext getChannel() {
		return channel.get();
	}
	public void setChannel(ChannelHandlerContext context) {
		this.channel.set(context);
	}
	public ExecutorService getEventExecutor() {
		return eventExecutor;
	}
	public ExecutorService getPushExecutor() {
		return pushExecutor;
	}
	public long getLastRecvTime() {
		return lastRecvTime;
	}
	public void setLastRecvTime(long lastRecvTime) {
		this.lastRecvTime = lastRecvTime;
	}
	public ConcurrentLinkedQueue<SessionRequest> getRequestQueue() {
		return requestQueue;
	}
}

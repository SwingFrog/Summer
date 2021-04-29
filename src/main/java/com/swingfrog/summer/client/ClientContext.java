package com.swingfrog.summer.client;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.protocol.SessionRequest;

import io.netty.channel.ChannelHandlerContext;

public class ClientContext {

	private final int id;
	private final ClientConfig config;
	private final Client client;
	private final AtomicReference<ChannelHandlerContext> channel = new AtomicReference<>();
	private volatile long lastRecvTime;
	private final ConcurrentLinkedQueue<SessionRequest> requestQueue = new ConcurrentLinkedQueue<>();
	
	public ClientContext(int id, ClientConfig config, Client client) {
		this.id = id;
		this.config = config;
		this.client = client;
		lastRecvTime = System.currentTimeMillis();
	}
	public int getId() {
		return id;
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
	public long getLastRecvTime() {
		return lastRecvTime;
	}
	public void setLastRecvTime(long lastRecvTime) {
		this.lastRecvTime = lastRecvTime;
	}
	public ConcurrentLinkedQueue<SessionRequest> getRequestQueue() {
		return requestQueue;
	}
	public boolean isChannelActive() {
		ChannelHandlerContext channelHandlerContext = channel.get();
		return channelHandlerContext != null && channelHandlerContext.channel().isActive();
	}
}

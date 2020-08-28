package com.swingfrog.summer.client;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.protocol.SessionRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

public class ClientContext {

	private ClientConfig config;
	private Client client;
	private final AtomicReference<ChannelHandlerContext> channel = new AtomicReference<>();
	private EventLoopGroup eventGroup;
	private EventLoopGroup pushGroup;
	private final AtomicInteger heartCount = new AtomicInteger(0);
	private final ConcurrentLinkedQueue<SessionRequest> requestQueue = new ConcurrentLinkedQueue<>();
	
	public ClientContext(ClientConfig config, Client client, EventLoopGroup eventGroup) {
		this.config = config;
		this.client = client;
		this.eventGroup = eventGroup;
		this.pushGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("ClientPush", true));
	}
	public ClientConfig getConfig() {
		return config;
	}
	public void setConfig(ClientConfig config) {
		this.config = config;
	}
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	public ChannelHandlerContext getChannel() {
		return channel.get();
	}
	public void setChannel(ChannelHandlerContext context) {
		this.channel.set(context);
	}
	public EventLoopGroup getEventGroup() {
		return eventGroup;
	}
	public void setEventGroup(EventLoopGroup eventGroup) {
		this.eventGroup = eventGroup;
	}
	public EventLoopGroup getPushGroup() {
		return pushGroup;
	}
	public void setPushGroup(EventLoopGroup pushGroup) {
		this.pushGroup = pushGroup;
	}
	public int getHeartCount() {
		return heartCount.get();
	}
	public void setHeartCount(int heartCount) {
		this.heartCount.set(heartCount);
	}
	public ConcurrentLinkedQueue<SessionRequest> getRequestQueue() {
		return requestQueue;
	}
}

package com.swingfrog.summer.server;

import com.google.common.collect.Queues;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class SessionContext {

	private final Channel channel;
	private final String sessionId;
	private String directAddress;
	private String realAddress;
	private int port;
	
	private volatile long currentMsgId;
	private volatile long lastRecvTime;
	private final ConcurrentLinkedQueue<Object> waitWriteQueue = Queues.newConcurrentLinkedQueue();

	private volatile Object token;

	private final ConcurrentMap<Object, Object> data = new ConcurrentHashMap<>();

	public SessionContext(Channel channel) {
		this.channel = channel;
		this.sessionId = channel.id().asLongText();
	}

	public String getSessionId() {
		return sessionId;
	}
	public String getDirectAddress() {
		return directAddress;
	}
	public void setDirectAddress(String directAddress) {
		this.directAddress = directAddress;
	}
	public String getRealAddress() {
		return realAddress;
	}
	public void setRealAddress(String realAddress) {
		this.realAddress = realAddress;
	}
	public String getAddress() {
		return realAddress != null ? realAddress : directAddress;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public long getCurrentMsgId() {
		return currentMsgId;
	}
	public void setCurrentMsgId(long currentMsgId) {
		this.currentMsgId = currentMsgId;
	}
	public long getLastRecvTime() {
		return lastRecvTime;
	}
	public void setLastRecvTime(long lastRecvTime) {
		this.lastRecvTime = lastRecvTime;
	}
	ConcurrentLinkedQueue<Object> getWaitWriteQueue() {
		return waitWriteQueue;
	}
	public int getWaitWriteQueueSize() {
		return waitWriteQueue.size();
	}

	@Override
	public String toString() {
		return String.format("IP[%s:%s]", getAddress(), port);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SessionContext that = (SessionContext) o;
		return sessionId.equals(that.sessionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sessionId);
	}

	@SuppressWarnings("unchecked")
	public <T> T getToken() {
		return (T) token;
	}

	public void setToken(Object token) {
		this.token = token;
	}

	public void clearToken() {
		token = null;
	}

	Channel getChannel() {
		return channel;
	}

	public ConcurrentMap<Object, Object> getData() {
		return data;
	}

	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	public void put(Object key, Object value) {
		data.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Object key) {
		return (T) data.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getOrDefault(Object key, T defaultValue) {
		Object value = data.get(key);
		if (value == null)
			return defaultValue;
		return (T) value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getOrSupplier(Object key, Supplier<T> supplier) {
		Object value = data.get(key);
		if (value == null)
			return supplier.get();
		return (T) value;
	}

	@SuppressWarnings("unchecked")
	public <T> T remove(Object key) {
		return (T) data.remove(key);
	}

	public void removeAll() {
		data.clear();
	}

	public boolean isActive() {
		return channel.isActive();
	}

	public ByteBufAllocator alloc() {
		return channel.alloc();
	}

}

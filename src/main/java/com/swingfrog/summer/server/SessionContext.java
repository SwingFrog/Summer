package com.swingfrog.summer.server;

import com.google.common.collect.Queues;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.async.AsyncResponseMgr;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SessionContext {

	private final String sessionId;
	private String directAddress;
	private String realAddress;
	private int port;
	
	private long currentMsgId;
	private final AtomicInteger heartCount = new AtomicInteger(0);
	private long lastRecvTime;
	private final ConcurrentLinkedQueue<String> waitWriteQueue = Queues.newConcurrentLinkedQueue();

	private Object token;

	public SessionContext(String sessionId) {
		this.sessionId = sessionId;
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
	public int getHeartCount() {
		return heartCount.get();
	}
	public void setHeartCount(int heartCount) {
		this.heartCount.set(heartCount);
	}
	public void incrementHeartCount() {
		heartCount.incrementAndGet();
	}
	public long getLastRecvTime() {
		return lastRecvTime;
	}
	public void setLastRecvTime(long lastRecvTime) {
		this.lastRecvTime = lastRecvTime;
	}
	ConcurrentLinkedQueue<String> getWaitWriteQueue() {
		return waitWriteQueue;
	}
	public int getWaitWriteQueueSize() {
		return waitWriteQueue.size();
	}
	public Object getToken() {
		return token;
	}
	public void setToken(Object token) {
		this.token = token;
	}
	public void clearToken() {
		token = null;
	}

	@Override
	public String toString() {
		return String.format("IP[%s:%s]", getAddress(), port);
	}
	public void send(SessionRequest request, Object data) {
		AsyncResponseMgr.get().sendResponse(this, request, data);
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

}

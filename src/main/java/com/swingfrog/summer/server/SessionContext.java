package com.swingfrog.summer.server;

import com.google.common.collect.Queues;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.async.AsyncResponseMgr;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SessionContext {

	private String sessionId;
	private String directAddress;
	private String realAddress;
	private int port;
	
	private long currentMsgId;
	private AtomicInteger heartCount = new AtomicInteger(0);
	private long lastRecvTime;
	private ConcurrentLinkedQueue<String> waitWriteQueue = Queues.newConcurrentLinkedQueue();
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public void clearSessionId() {
		sessionId = null;
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
	@Override
	public String toString() {
		return String.format("IP[%s:%s]", getAddress(), port);
	}
	public void send(SessionRequest request, Object data) {
		AsyncResponseMgr.get().sendResponse(this, request, data);
	}
}

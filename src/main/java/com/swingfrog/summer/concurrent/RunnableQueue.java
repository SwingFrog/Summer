package com.swingfrog.summer.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RunnableQueue {

	private AtomicBoolean state;
	private ConcurrentLinkedQueue<Runnable> queue;
	
	public static RunnableQueue build() {
		RunnableQueue rq = new RunnableQueue();
		rq.setState(new AtomicBoolean(true));
		rq.setQueue(new ConcurrentLinkedQueue<>());
		return rq;
	}
	public AtomicBoolean getState() {
		return state;
	}
	public void setState(AtomicBoolean state) {
		this.state = state;
	}
	public ConcurrentLinkedQueue<Runnable> getQueue() {
		return queue;
	}
	public void setQueue(ConcurrentLinkedQueue<Runnable> queue) {
		this.queue = queue;
	}
	
}

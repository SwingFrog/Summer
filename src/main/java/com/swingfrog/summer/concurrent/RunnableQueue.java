package com.swingfrog.summer.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RunnableQueue {

	public static final int STATUS_WAIT = 1;
	public static final int STATUS_INTEND = 2;
	public static final int STATUS_RUNNING = 3;

	private final AtomicInteger status = new AtomicInteger(STATUS_WAIT);
	private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
	
	public static RunnableQueue build() {
		return new RunnableQueue();
	}

	public AtomicInteger getStatus() {
		return status;
	}

	public ConcurrentLinkedQueue<Runnable> getQueue() {
		return queue;
	}
	
}

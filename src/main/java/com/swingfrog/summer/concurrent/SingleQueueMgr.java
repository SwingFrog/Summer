package com.swingfrog.summer.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleQueueMgr {

	private static final Logger log = LoggerFactory.getLogger(SingleQueueMgr.class);
	
	private ExecutorService eventExecutor;
	private final Map<Object, RunnableQueue> singleQueueMap;
	
	private static class SingleCase {
		public static final SingleQueueMgr INSTANCE = new SingleQueueMgr();
	}
	
	private SingleQueueMgr() {
		singleQueueMap = new HashMap<>();
	}
	
	public static SingleQueueMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init(ExecutorService eventExecutor) {
		this.eventExecutor = eventExecutor;
	}
	
	public RunnableQueue getRunnableQueue(Object key) {
		if (key == null) {
			throw new NullPointerException("key is null");
		}
		RunnableQueue rq = singleQueueMap.get(key);
		if (rq == null) {
			synchronized (key) {
				rq = singleQueueMap.get(key);
				if (rq == null) {
					rq = RunnableQueue.build();
					singleQueueMap.put(key, rq);
				}
			}
		}
		return rq;
	}
	
	public void execute(Object key, Runnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable is null");
		}
		log.debug("SingleQueueMgr execute runnable key[{}]", key);
		getRunnableQueue(key).getQueue().add(runnable);
		next(key);
	}
	
	public void next(Object key) {
		RunnableQueue rq = getRunnableQueue(key);
		if (rq.getState().compareAndSet(true, false)) {
			Runnable runnable = rq.getQueue().poll();
			if (runnable != null) {
				eventExecutor.execute(()->{
					try {
						runnable.run();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					} finally {
						finish(key);
					}
				});
			} else {
				rq.getState().compareAndSet(false, true);
			}
		}
	}
	
	public void finish(Object key) {
		RunnableQueue rq = getRunnableQueue(key);
		rq.getState().compareAndSet(false, true);
		next(key);
	}
}

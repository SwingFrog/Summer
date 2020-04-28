package com.swingfrog.summer.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.server.SessionContext;

public class SessionQueueMgr {

	private static final Logger log = LoggerFactory.getLogger(SessionQueueMgr.class);
	
	private ExecutorService eventExecutor;
	private Map<SessionContext, RunnableQueue> singleQueueMap;
	
	private static class SingleCase {
		public static final SessionQueueMgr INSTANCE = new SessionQueueMgr();
	}
	
	private SessionQueueMgr() {
		singleQueueMap = new HashMap<>();
	}
	
	public static SessionQueueMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init(ExecutorService eventExecutor) {
		this.eventExecutor = eventExecutor;
	}
	
	public RunnableQueue getRunnableQueue(SessionContext key) {
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
	
	public void shutdown(SessionContext key) {
		if (key == null) {
			throw new NullPointerException("key is null");
		}
		singleQueueMap.remove(key);
	}
	
	public int getQueueSize(SessionContext key) {
		return getRunnableQueue(key).getQueue().size();
	}
	
	public void execute(SessionContext key, Runnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable is null");
		}
		log.debug("SessionQueueMgr execute runnable key[{}]", key);
		getRunnableQueue(key).getQueue().add(runnable);
		next(key);
	}
	
	public void next(SessionContext key) {
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
	
	public void finish(SessionContext key) {
		RunnableQueue rq = getRunnableQueue(key);
		rq.getState().compareAndSet(false, true);
		next(key);
	}
	
}

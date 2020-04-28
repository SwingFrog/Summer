package com.swingfrog.summer.event;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.annotation.BindEvent;
import com.swingfrog.summer.ioc.ContainerMgr;

public class EventBusMgr {

	private static final Logger log = LoggerFactory.getLogger(EventBusMgr.class);
	private Map<Method, Class<?>> methodMap;
	private Map<String, List<EventMethod>> eventMap;
	private ExecutorService eventExecutor;
	
	private static class SingleCase {
		public static final EventBusMgr INSTANCE = new EventBusMgr();
	}
	
	private EventBusMgr() {
		methodMap = Maps.newHashMap();
		eventMap = Maps.newHashMap();
		eventExecutor = Executors.newSingleThreadExecutor(new DefaultThreadFactory("EventBus"));
	}
	
	public static EventBusMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init() {
		log.info("event bus init");
		Iterator<Class<?>> ite = ContainerMgr.get().iteratorEventList();
		while (ite.hasNext()) {
			Class<?> clazz = ite.next();
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				BindEvent event = method.getDeclaredAnnotation(BindEvent.class);
				if (event != null) {
					methodMap.put(method, clazz);
					List<EventMethod> eventList = eventMap.computeIfAbsent(event.value(), k -> Lists.newLinkedList());
					eventList.add(new EventMethod(method, event.index()));
				}
			}
		}
		for (String eventName : eventMap.keySet()) {
			List<EventMethod> eventList = eventMap.get(eventName);
			eventList.sort(Comparator.comparing(EventMethod::getIndex));
			log.info("event name {}", eventName);
			for (EventMethod event : eventList) {
				Class<?> clazz = methodMap.get(event.getMethod());
				log.info("event register event handler {}.{} index[{}]", clazz.getSimpleName(), event.getMethod().getName(), event.getIndex());
			}
		}
	}
	
	private void dispatch(String eventName, Object ...args) {
		log.debug("dispatch event[{}]", eventName);
		List<EventMethod> eventList = eventMap.get(eventName);
		if (eventList == null) {
			log.warn("event handler {} not exist", eventName);
		} else {
			for (EventMethod event : eventList) {
				Class<?> clazz = methodMap.get(event.getMethod());
				Object obj = ContainerMgr.get().getDeclaredComponent(clazz);
				log.debug("dispatch event[{}] invoke {}.{}", eventName, clazz.getSimpleName(), event.getMethod().getName());
				try {
					if (event.getMethod().invoke(obj, args) != null) {
						break;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	public void syncDispatch(String eventName, Object ...args) {
		dispatch(eventName, args);
	}
	
	public void asyncDispatch(String eventName, Object ...args) {
		eventExecutor.execute(() -> dispatch(eventName, args));
	}

	public void shutdown() {
		log.info("event bus shutdown");
		eventExecutor.shutdown();
		try {
			while (!eventExecutor.isTerminated()) {
				eventExecutor.awaitTermination(1, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e){
			log.error(e.getMessage(), e);
		}
	}
	
	private static class EventMethod {
		private Method method;
		private int index;
		public EventMethod(Method method, int index) {
			this.method = method;
			this.index = index;
		}
		public Method getMethod() {
			return method;
		}
		public int getIndex() {
			return index;
		}
	}
	
}

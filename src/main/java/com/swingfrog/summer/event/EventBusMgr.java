package com.swingfrog.summer.event;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swingfrog.summer.annotation.AcceptEvent;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.annotation.BindEvent;
import com.swingfrog.summer.ioc.ContainerMgr;

public class EventBusMgr {

	private static final Logger log = LoggerFactory.getLogger(EventBusMgr.class);
	private Map<String, List<EventMethod>> eventNameMap;
	private Map<Class<?>, List<EventMethod>> eventClassMap;

	private final ExecutorService eventExecutor;
	
	private static class SingleCase {
		public static final EventBusMgr INSTANCE = new EventBusMgr();
	}
	
	private EventBusMgr() {
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
				BindEvent bindEvent = method.getDeclaredAnnotation(BindEvent.class);
				if (bindEvent != null) {
					if (eventNameMap == null)
						eventNameMap = Maps.newHashMap();
					List<EventMethod> eventList = eventNameMap.computeIfAbsent(bindEvent.value(), k -> Lists.newArrayList());
					eventList.add(new EventMethod(clazz, method, bindEvent.index()));
					continue;
				}

				AcceptEvent acceptEvent = method.getDeclaredAnnotation(AcceptEvent.class);
				if (acceptEvent == null)
					continue;
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length != 1)
					throw new EventBusRuntimeException("event handler only one param -> %s.%s", clazz.getSimpleName(), method.getName());
				if (eventClassMap == null)
					eventClassMap = Maps.newHashMap();
				Class<?> parameterType = parameterTypes[0];
				List<EventMethod> eventList = eventClassMap.computeIfAbsent(parameterType, k -> Lists.newArrayList());
				eventList.add(new EventMethod(clazz, method, acceptEvent.index()));
			}
		}
		if (eventNameMap != null) {
			for (String eventName : eventNameMap.keySet()) {
				List<EventMethod> eventList = eventNameMap.get(eventName);
				eventList.sort(Comparator.comparing(EventMethod::getIndex));
				log.info("event name {}", eventName);
				for (EventMethod event : eventList) {
					log.info("event register event handler {}.{} index[{}]",
							event.getClazz().getSimpleName(), event.getMethod().getName(), event.getIndex());
				}
			}
		}
		if (eventClassMap != null) {
			for (Class<?> eventClass : eventClassMap.keySet()) {
				List<EventMethod> eventList = eventClassMap.get(eventClass);
				log.info("event class {}", eventClass.getName());
				for (EventMethod event : eventList) {
					log.info("event register event handler {}.{} index[{}]",
							event.getClazz().getSimpleName(), event.getMethod().getName(), event.getIndex());
				}
			}
		}
	}

	private void dispatch(String eventName, Object ...args) {
		if (log.isDebugEnabled())
			log.debug("dispatch event[{}]", eventName);
		if (eventNameMap == null) {
			log.warn("event handler {} not exist", eventName);
			return;
		}
		List<EventMethod> eventList = eventNameMap.get(eventName);
		if (eventList == null) {
			log.warn("event handler {} not exist", eventName);
			return;
		}
		for (EventMethod event : eventList) {
			Class<?> clazz = event.getClazz();
			Object obj = ContainerMgr.get().getDeclaredComponent(clazz);
			if (log.isDebugEnabled())
				log.debug("dispatch event[{}] invoke {}.{}",
						eventName, clazz.getSimpleName(), event.getMethod().getName());
			try {
				if (event.getMethod().invoke(obj, args) != null) {
					break;
				}
			} catch (Exception e) {
				log.error("dispatch event[{}] invoke {}.{} failure",
						eventName, clazz.getSimpleName(), event.getMethod().getName());
				log.error(e.getMessage(), e);
			}
		}
	}

	private void post(Object eventObj) {
		if (log.isDebugEnabled())
			log.debug("dispatch event[{}]", eventObj.getClass().getSimpleName());
		if (eventClassMap == null) {
			log.warn("event handler {} not exist", eventObj.getClass().getSimpleName());
			return;
		}
		List<EventMethod> eventList = eventClassMap.get(eventObj.getClass());
		if (eventList == null) {
			log.warn("event handler {} not exist", eventObj.getClass().getSimpleName());
			return;
		}
		for (EventMethod event : eventList) {
			Class<?> clazz = event.getClazz();
			Object obj = ContainerMgr.get().getDeclaredComponent(clazz);
			if (log.isDebugEnabled())
				log.debug("dispatch event[{}] invoke {}.{}",
						eventObj.getClass().getSimpleName(), clazz.getSimpleName(), event.getMethod().getName());
			try {
				if (event.getMethod().invoke(obj, eventObj) != null) {
					break;
				}
			} catch (Exception e) {
				log.error("dispatch event[{}] invoke {}.{} failure",
						eventObj.getClass().getSimpleName(), clazz.getSimpleName(), event.getMethod().getName());
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void syncDispatch(String eventName, Object ...args) {
		dispatch(eventName, args);
	}
	
	public void asyncDispatch(String eventName, Object ...args) {
		eventExecutor.execute(() -> dispatch(eventName, args));
	}

	public void syncDispatch(Object event) {
		post(event);
	}

	public void asyncDispatch(Object event) {
		eventExecutor.execute(() -> post(event));
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
		private final Class<?> clazz;
		private final Method method;
		private final int index;
		public EventMethod(Class<?> clazz, Method method, int index) {
			this.clazz = clazz;
			this.method = method;
			this.index = index;
		}
		public Class<?> getClazz() {
			return clazz;
		}
		public Method getMethod() {
			return method;
		}
		public int getIndex() {
			return index;
		}
	}
	
}

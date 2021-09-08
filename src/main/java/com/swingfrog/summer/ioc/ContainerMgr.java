package com.swingfrog.summer.ioc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer.annotation.*;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.annotation.base.AutowiredManager;
import com.swingfrog.summer.annotation.base.ComponentManager;
import com.swingfrog.summer.annotation.base.MethodParameter;
import com.swingfrog.summer.annotation.base.QueueManager;
import com.swingfrog.summer.annotation.base.SynchronizedManager;
import com.swingfrog.summer.annotation.base.TransactionManager;
import com.swingfrog.summer.concurrent.MatchGroupKey;
import com.swingfrog.summer.proxy.ProxyUtil;
import com.swingfrog.summer.task.MethodInvoke;
import com.swingfrog.summer.task.TaskMgr;
import com.swingfrog.summer.task.TaskTrigger;
import com.swingfrog.summer.task.TaskUtil;

public class ContainerMgr {

	private static final Logger log = LoggerFactory.getLogger(ContainerMgr.class);
	
	private final Map<Class<?>, Object> map = Maps.newHashMap();
	private final List<Class<?>> autowiredList = Lists.newArrayList();
	private final Set<Class<?>> componentSet = Sets.newHashSet();
	private final Set<Method> transactionSet = Sets.newHashSet();
	private final Set<Class<?>> parameterSet = Sets.newHashSet();
	private final List<Class<?>> remoteList = Lists.newArrayList();
	private final List<Class<?>> pushList = Lists.newArrayList();
	private final List<Class<?>> handlerList = Lists.newArrayList();
	private final Map<String, List<Class<?>>> handlerMap = Maps.newHashMap();
	private final Map<Method, MatchGroupKey> singleQueueMap = Maps.newHashMap();
	private final Map<Method, String> synchronizedMap = Maps.newHashMap();
	private final List<TaskTrigger> taskList = Lists.newArrayList();
	private final List<Class<?>> eventList = Lists.newArrayList();
	
	private static class SingleCase {
		public static final ContainerMgr INSTANCE = new ContainerMgr();
	}
	
	private ContainerMgr() {

	}
	
	public static ContainerMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init(String projectPackage) throws InstantiationException, IllegalAccessException {
		log.info("container init...");
		Set<Class<?>> classSet = ClassFind.getClasses(projectPackage);
		for (Class<?> clazz : classSet) {
			if (!clazz.isAnonymousClass() && 
					!clazz.isMemberClass() && 
					!clazz.isLocalClass() && 
					!clazz.isAnnotation() && 
					!clazz.isInterface()) {
				analysis(clazz, clazz);
			}
		}
	}

	private void analysis(Class<?> clazz, Class<?> anno) throws InstantiationException, IllegalAccessException {
		if (anno.isAnnotationPresent(Component.class)) {
			log.info("register component {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			analysis(clazz, Component.class);
		}
		if (anno.isAnnotationPresent(Bean.class)) {
			log.info("register bean {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			analysis(clazz, Bean.class);
		}
		if (anno.isAnnotationPresent(Dao.class)) {
			log.info("register dao {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			analysis(clazz, Dao.class);
		} 
		if (anno.isAnnotationPresent(ServerHandler.class)) {
			log.info("register server handler {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			ServerHandler sh = anno.getAnnotation(ServerHandler.class);
			if (sh == null || sh.value().isEmpty()) {
				handlerList.add(clazz);
			} else {
				List<Class<?>> list = handlerMap.computeIfAbsent(sh.value(), k -> Lists.newArrayList());
				list.add(clazz);
			}
			analysis(clazz, ServerHandler.class);
		} 
		if (anno.isAnnotationPresent(Service.class)) {
			log.info("register service {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			analysis(clazz, Service.class);
		} 
		if (anno.isAnnotationPresent(Remote.class)) {
			log.info("register remote {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			remoteList.add(clazz);
			analysis(clazz, Remote.class);
		}
		if (anno.isAnnotationPresent(Push.class)) {
			log.info("register push {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			pushList.add(clazz);
			analysis(clazz, Push.class);
		}
		if (anno.isAnnotationPresent(Task.class)) {
			Object obj = clazz.newInstance();
			map.put(clazz, obj);
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				CronTask cronTask = method.getDeclaredAnnotation(CronTask.class);
				if (cronTask != null) {
					log.info("register cron[{}] task {}.{}", cronTask.value(), clazz.getSimpleName(), method.getName());
					MethodInvoke methodInvoke = ProxyUtil.getProxyRemote(new MethodInvoke());
					methodInvoke.init(obj, method);
					taskList.add(TaskUtil.getCronTask(cronTask.value(), methodInvoke));
				} else {
					IntervalTask intervalTask = method.getDeclaredAnnotation(IntervalTask.class);
					if (intervalTask != null) {
						log.info("register interval[{}] task {}.{}", intervalTask.value(), clazz.getSimpleName(), method.getName());
						MethodInvoke methodInvoke = ProxyUtil.getProxyRemote(new MethodInvoke());
						methodInvoke.init(obj, method);
						taskList.add(TaskUtil.getIntervalTask(intervalTask.value(), intervalTask.delay(), methodInvoke));
					}
				}
			}
			analysis(clazz, Task.class);
		} 
		if (anno.isAnnotationPresent(AutowiredManager.class)) {
			autowiredList.add(clazz);
		} 
		if (anno.isAnnotationPresent(ComponentManager.class)) {
			componentSet.add(clazz);
		} 
		if (anno.isAnnotationPresent(MethodParameter.class)) {
			parameterSet.add(clazz);
		} 
		if (anno.isAnnotationPresent(TransactionManager.class)) {
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(Transaction.class)) {
					log.info("open transaction {}.{}", clazz.getSimpleName(), method.getName());
					transactionSet.add(method);
				}
			}
		}
		if (anno.isAnnotationPresent(QueueManager.class)) {
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				SingleQueue singleQueue = method.getDeclaredAnnotation(SingleQueue.class);
				if (singleQueue != null) {
					log.info("open single queue[{}] {}.{}", singleQueue.value(), clazz.getSimpleName(), method.getName());
					singleQueueMap.put(method, new MatchGroupKey(singleQueue.value()));
				}
			}
		}
		if (anno.isAnnotationPresent(SynchronizedManager.class)) {
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				Synchronized sync = method.getDeclaredAnnotation(Synchronized.class);
				if (sync != null) {
					log.info("open synchronized[{}] {}.{}", sync.value(), clazz.getSimpleName(), method.getName());
					synchronizedMap.put(method, sync.value());
				}
			}
		}
		if (anno.isAnnotationPresent(EventHandler.class)) {
			log.info("register event handler {}", clazz.getSimpleName());
			map.put(clazz, clazz.newInstance());
			eventList.add(clazz);
			analysis(clazz, EventHandler.class);
		}
	}
	
	public void autowired() throws IllegalArgumentException, IllegalAccessException {
		for (Class<?> clazz : autowiredList) {
			autowired(map.get(clazz));
		}
	}
	
	public void autowired(Object obj) throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = obj.getClass();
		while (clazz != null) {
			autowired(obj, clazz);
			clazz = clazz.getSuperclass();
		}
	}

	private void autowired(Object obj, Class<?> clazz) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Autowired.class)) {
				Class<?> type = field.getType();
				if (componentSet.contains(type)) {
					log.info("autowired {}.{} success", clazz.getSimpleName(), field.getName());
					field.setAccessible(true);
					field.set(obj, map.get(type));
				} else {
					log.info("autowired {}.{} fail", clazz.getSimpleName(), field.getName());
				}
			}
		}
	}
	
	public void proxyObj() {
		for (Class<?> clazz : map.keySet()) {
			if (clazz.isAnnotationPresent(Service.class)) {
				log.info("proxy object {}", clazz.getSimpleName());
				map.put(clazz, ProxyUtil.getProxyService(map.get(clazz)));
			}
			if (clazz.isAnnotationPresent(Remote.class)) {
				log.info("proxy object {}", clazz.getSimpleName());
				map.put(clazz, ProxyUtil.getProxyRemote(map.get(clazz)));
			}
			if (clazz.isAnnotationPresent(EventHandler.class)) {
				log.info("proxy object {}", clazz.getSimpleName());
				map.put(clazz, ProxyUtil.getProxyRemote(map.get(clazz)));
			}
		}
	}
	
	public void startTask() throws SchedulerException {
		for (TaskTrigger taskTrigger : taskList) {
			TaskMgr.get().start(taskTrigger);
		}
	}

	public void addComponent(Object obj) {
		map.put(obj.getClass(), obj);
		componentSet.add(obj.getClass());
	}
	
	public void removeComponent(Object obj) {
		map.remove(obj.getClass());
		componentSet.remove(obj.getClass());
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> clazz) {
		if (componentSet.contains(clazz)) {
			return (T)map.get(clazz);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> listDeclaredComponent(Class<T> clazz) {
		return map.values().stream().filter(clazz::isInstance).map(obj -> (T)obj).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <T> T getDeclaredComponent(Class<T> clazz) {
		return (T)map.get(clazz);
	}
	
	public boolean containsComponent(Object obj) {
		return componentSet.contains(obj.getClass());
	}
	
	public boolean isTransaction(Method method) {
		return transactionSet.contains(method);
	}
	
	public boolean isAutowiredParameter(Class<?> clazz) {
		return parameterSet.contains(clazz);
	}
	
	public Iterator<Class<?>> iteratorRemoteList() {
		return remoteList.iterator();
	}
	
	public Iterator<Class<?>> iteratorPushList() {
		return pushList.iterator();
	}
	
	public Iterator<Class<?>> iteratorHandlerList() {
		return handlerList.iterator();
	}
	
	public Iterator<Class<?>> iteratorHandlerList(String serverName) {
		if (handlerMap.containsKey(serverName)) {
			return handlerMap.get(serverName).iterator();
		}
		return null;
	}
	
	public MatchGroupKey getSingleQueueKey(Method method) {
		return singleQueueMap.get(method);
	}
	
	public String getSynchronizedName(Method method) {
		return synchronizedMap.get(method);
	}

	public Iterator<Class<?>> iteratorEventList() {
		return eventList.iterator();
	}

}

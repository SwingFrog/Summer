package com.swingfrog.summer.client;

import java.lang.reflect.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.swingfrog.summer.util.JSONConvertUtil;
import com.swingfrog.summer.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.swingfrog.summer.annotation.Optional;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.ioc.MethodParameterName;
import com.swingfrog.summer.protocol.SessionResponse;

import javassist.NotFoundException;

public class PushDispatchMgr {

	private static final Logger log = LoggerFactory.getLogger(PushDispatchMgr.class);
	private final Map<String, PushClass> pushClassMap;
	private final ConcurrentMap<Long, SessionResponse> syncRemote;
	private final ConcurrentMap<Long, RemoteCallback> asyncRemote;
	private final Set<Long> syncRemoteDiscard;
	
	private static class SingleCase {
		public static final PushDispatchMgr INSTANCE = new PushDispatchMgr();
	}
	
	private PushDispatchMgr() {
		pushClassMap = Maps.newHashMap();
		syncRemote = Maps.newConcurrentMap();
		asyncRemote = Maps.newConcurrentMap();
		syncRemoteDiscard = Sets.newConcurrentHashSet();
	}
	
	public static PushDispatchMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init() throws NotFoundException {
		Iterator<Class<?>> ite = ContainerMgr.get().iteratorPushList();
		while (ite.hasNext()) {
			Class<?> clazz = ite.next();
			log.info("client register remote {}", clazz.getSimpleName());
			pushClassMap.put(clazz.getSimpleName(), new PushClass(clazz));
		}
	}
	
	public void processPush(SessionResponse sessionResponse) {
		log.debug("client push {}", sessionResponse.toJSONString());
		String push = sessionResponse.getRemote();
		String method = sessionResponse.getMethod();
		JSONObject data = (JSONObject) sessionResponse.getData();
		PushClass pushClass = pushClassMap.get(push);
		if (pushClass == null) {
			log.error("client push[{}] method[{}] not found", push, method);
			return;
		}
		PushMethod pushMethod = pushClass.getPushMethod(method);
		if (pushMethod == null) {
			log.error("client push[{}] method[{}] not found", push, method);
			return;
		}
		Object remoteObj = ContainerMgr.get().getDeclaredComponent(pushClass.getClazz());
		Method remoteMod = pushMethod.getMethod();
		String[] params = pushMethod.getParams();
		Type[] paramTypes = pushMethod.paramTypes();
		Parameter[] parameters = pushMethod.getParameters();
		boolean auto = ContainerMgr.get().isAutowiredParameter(pushClass.getClazz());
		Object[] obj = new Object[params.length];
		for (int i = 0; i < params.length; i++) {
			String param = params[i];
			Type type = paramTypes[i];
			Parameter parameter = parameters[i];
			if (JSONConvertUtil.containsType(type)) {
				obj[i] = JSONConvertUtil.convert(type, data, param);
			} else {
				if (data.containsKey(param)) {
					try {
						obj[i] = JSON.parseObject(data.getString(param), type);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				} else {
					if (auto) {
						obj[i] = ContainerMgr.get().getComponent((Class<?>) type);
						if (obj[i] == null) {
							try {
								obj[i] = ((Class<?>) type).newInstance();
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
						}
					}
				}
			}
			if (obj[i] == null) {
				Optional optional = parameter.getAnnotation(Optional.class);
				if (optional != null) {
					obj[i] = ParamUtil.convert(type, optional.value());
				} else {
					log.error("client push[{}] method[{}] parameter[{}] error", push, method, data);
					return;
				}
			}
		}
		try {
			remoteMod.invoke(remoteObj, obj);
		} catch (InvocationTargetException e) {
			log.error(e.getTargetException().getMessage(), e.getTargetException());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void processRemote(SessionResponse sessionResponse) {
		log.debug("client response {}", sessionResponse.toJSONString());
		if (asyncRemote.containsKey(sessionResponse.getId())) {
			ScheduledFuture<?> scheduledFuture = ClientMgr.get().getFutureMap().remove(sessionResponse.getId());
			if (scheduledFuture != null) {
				scheduledFuture.cancel(false);
			}
			RemoteCallback remoteCallback = asyncRemote.remove(sessionResponse.getId());
			if (remoteCallback == null) {
				return;
			}
			if (sessionResponse.getCode() != 0) {
				remoteCallback.failure(sessionResponse.getCode(), sessionResponse.getData().toString());
			} else {
				if (remoteCallback instanceof RemoteCallbackQuick) {
					RemoteCallbackQuick<?> remoteCallbackQuick = (RemoteCallbackQuick<?>) remoteCallback;
					remoteCallbackQuick.success(parseData(sessionResponse.getData(), parseClass(remoteCallbackQuick)));
				} else {
					remoteCallback.success(sessionResponse.getData());
				}
			}
		} else {
			if (syncRemoteDiscard.remove(sessionResponse.getId())) {
				log.warn("client discard response id[{}]", sessionResponse.getId());
			} else {
				syncRemote.put(sessionResponse.getId(), sessionResponse);
			}
		}
	}

	private Object parseData(Object resp, Type type) {
		if (resp != null) {
			String res = resp.toString();
			if (type == boolean.class || type == Boolean.class) {
				return Boolean.valueOf(res);
			} else if (type == byte.class || type == Byte.class) {
				return Byte.parseByte(res);
			} else if (type == short.class || type == Short.class) {
				return Short.parseShort(res);
			} else if (type == int.class || type == Integer.class) {
				return Integer.parseInt(res);
			} else if (type == long.class || type == Long.class) {
				return Long.parseLong(res);
			} else if (type == String.class) {
				return res;
			} else {
				return JSON.parseObject(res, type);
			}
		}
		return null;
	}

	private Type parseClass(Object object) {
		Class<?> clazz = object.getClass();
		Type superClass = clazz.getGenericSuperclass();
		if (superClass instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) superClass;
			Type[] typeArgs = parameterizedType.getActualTypeArguments();
			if (typeArgs != null && typeArgs.length > 0) {
				return typeArgs[0];
			}
		}
		return null;
	}
	
	private static class PushClass {
		private final Class<?> clazz;
		private final Map<String, PushMethod> pushMethodMap = Maps.newHashMap();
		public PushClass(Class<?> clazz) throws NotFoundException {
			this.clazz = clazz;
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				pushMethodMap.put(method.getName(), new PushMethod(method, new MethodParameterName(clazz)));
			}
		}
		public Class<?> getClazz() {
			return clazz;
		}
		public PushMethod getPushMethod(String method) {
			return pushMethodMap.get(method);
		}
	}
	
	private static class PushMethod {
		private final Method method;
		private final String[] params;
		private final Type[] paramTypes;
		private final Parameter[] parameters;
		public PushMethod(Method method, MethodParameterName mpn) throws NotFoundException {
			this.method = method;
			paramTypes = method.getGenericParameterTypes();
			params = mpn.getParameterNameByMethod(method);
			parameters = method.getParameters();
		}
		public Method getMethod() {
			return method;
		}
		public String[] getParams() {
			return params;
		}
		public Type[] paramTypes() {
			return paramTypes;
		}
		public Parameter[] getParameters() {
			return parameters;
		}
	}
	
	public void putAsyncRemote(long id, RemoteCallback callback) {
		asyncRemote.put(id, callback);
	}

	public boolean containsAsyncRemote(long id) {
		return asyncRemote.containsKey(id);
	}
	
	public SessionResponse getAndRemoveSyncRemote(long id) {
		return syncRemote.remove(id);
	}

	public void discardSyncRemote(long id) {
		syncRemoteDiscard.add(id);
	}
}

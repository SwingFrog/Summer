package com.swingfrog.summer.server;

import java.lang.reflect.*;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.swingfrog.summer.annotation.RequestMapping;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.exception.RemoteRuntimeException;
import com.swingfrog.summer.struct.AutowireParam;
import com.swingfrog.summer.util.JSONConvertUtil;
import com.swingfrog.summer.util.ProtobufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.swingfrog.summer.annotation.Optional;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.ioc.MethodParameterName;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.TextView;
import com.swingfrog.summer.web.view.WebView;

import javassist.NotFoundException;

public class RemoteDispatchMgr {
	
	private static final Logger log = LoggerFactory.getLogger(RemoteDispatchMgr.class);
	private final Map<String, RemoteMethod> remoteMethodMap;

	private static class SingleCase {
		public static final RemoteDispatchMgr INSTANCE = new RemoteDispatchMgr();
	}
	
	private RemoteDispatchMgr() {
		remoteMethodMap = Maps.newHashMap();
	}
	
	public static RemoteDispatchMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init() throws NotFoundException {
		Iterator<Class<?>> ite = ContainerMgr.get().iteratorRemoteList();
		while (ite.hasNext()) {
			Class<?> clazz = ite.next();
			log.info("server try register remote {}", clazz.getSimpleName());
			RemoteClass remoteClass = new RemoteClass(clazz);
			Method[] methods = clazz.getDeclaredMethods();
			MethodParameterName mpn = new MethodParameterName(clazz);
			for (Method method : methods) {
				if (method.getModifiers() != Modifier.PUBLIC || ProtobufUtil.hasProtobufParam(method)) {
					continue;
				}
				RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
				String remoteMethod;
				if (requestMapping != null) {
					remoteMethod = requestMapping.value();
				} else {
					remoteMethod = mergeRemoteMethod(clazz.getSimpleName(), method.getName());
				}
				if (remoteMethodMap.putIfAbsent(remoteMethod, new RemoteMethod(remoteClass, method, mpn)) == null) {
					if (requestMapping != null) {
						log.info("remote register {}.{} -> {}", clazz.getSimpleName(), method.getName(), remoteMethod);
					} else {
						log.info("remote register {}.{}", clazz.getSimpleName(), method.getName());
					}
				} else {
					throw new RemoteRuntimeException("remote register repeat %s.%s %s", clazz.getSimpleName(), method.getName(), remoteMethod);
				}
			}
		}
	}

	private String mergeRemoteMethod(String remote, String method) {
		if (method == null || method.isEmpty())
			return remote;
		if (remote == null || remote.isEmpty())
			return method;
		return remote + "." + method;
	}
	
	public Method getMethod(SessionRequest req) {
		String remote = req.getRemote();
		String method = req.getMethod();
		RemoteMethod remoteMethod = remoteMethodMap.get(mergeRemoteMethod(remote, method));
		if (remoteMethod == null)
			return null;
		return remoteMethod.getMethod();
	}

	public boolean containsRemoteMethod(String remoteMethod) {
		return remoteMethodMap.containsKey(remoteMethod);
	}
	
	private Object invoke(ServerContext serverContext, String remote, String method, JSONObject data, AutowireParam autowireParam) throws Throwable {
		Map<Class<?>, Object> objForTypes = autowireParam.getTypes();
		Map<String, Object> objForNames = autowireParam.getNames();
		RemoteMethod remoteMethod = remoteMethodMap.get(mergeRemoteMethod(remote, method));
		if (remoteMethod == null) {
			throw new CodeException(SessionException.METHOD_NOT_EXIST);
		}
		RemoteClass remoteClass = remoteMethod.getRemoteClass();
		if (remoteClass.isFilter() && !remoteClass.getServerName().equals(serverContext.getConfig().getServerName())) {
			throw new CodeException(SessionException.REMOTE_WAS_PROTECTED);
		}
		Object remoteObj = ContainerMgr.get().getDeclaredComponent(remoteClass.getClazz());
		Method remoteMod = remoteMethod.getMethod();
		String[] params = remoteMethod.getParams();
		Parameter[] parameters = remoteMethod.getParameters();
		boolean auto = ContainerMgr.get().isAutowiredParameter(remoteClass.getClazz());
		Object[] obj = new Object[params.length];
		try {
			for (int i = 0; i < parameters.length; i++) {
				String param = params[i];
				Parameter parameter = parameters[i];
				Type type = parameter.getParameterizedType();
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
							Class<?> typeClazz = parameter.getType();
							if (objForTypes != null && objForTypes.containsKey(typeClazz)) {
								obj[i] = objForTypes.get(typeClazz);
							} else if (objForNames != null && objForNames.containsKey(param)) {
								obj[i] = objForNames.get(param);
							} else {
								obj[i] = ContainerMgr.get().getComponent(typeClazz);
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
				}
				if (obj[i] == null) {
					if (!parameter.isAnnotationPresent(Optional.class)) {
						throw new CodeException(SessionException.PARAMETER_ERROR);
					}
				}
			}
		} catch (Exception e) {
			throw new CodeException(SessionException.PARAMETER_ERROR);
		}
		try {
			return remoteMod.invoke(remoteObj, obj);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	public ProcessResult<SessionResponse> process(ServerContext serverContext, SessionRequest req, SessionContext sctx,
												  AutowireParam autowireParam) throws Throwable {
		String remote = req.getRemote();
		String method = req.getMethod();
		JSONObject data = req.getData();
		Map<Class<?>, Object> objForTypes = autowireParam.getTypes();
		objForTypes.putIfAbsent(SessionContext.class, sctx);
		objForTypes.putIfAbsent(SessionRequest.class, req);
		Object result = invoke(serverContext, remote, method, data, autowireParam);
		if (result instanceof AsyncResponse) {
			return new ProcessResult<>(true, null);
		}
		return new ProcessResult<>(false, SessionResponse.buildMsg(req, result));
	}
	
	public ProcessResult<WebView> webProcess(ServerContext serverContext, WebRequest req, SessionContext sctx,
											 AutowireParam autowireParam) throws Throwable {
		String remote = req.getRemote();
		String method = req.getMethod();
		JSONObject data = req.getData();
		Map<Class<?>, Object> objForTypes = autowireParam.getTypes();
		Map<String, Object> objForNames = autowireParam.getNames();
		objForTypes.putIfAbsent(SessionContext.class, sctx);
		objForTypes.putIfAbsent(SessionRequest.class, req);
		for (String key : req.getFileUploadMap().keySet()) {
			objForNames.putIfAbsent(key, req.getFileUploadMap().get(key));
		}
		Object result = invoke(serverContext, remote, method, data, autowireParam);
		if (result instanceof AsyncResponse) {
			return new ProcessResult<>(true, null);
		}
		if (result == null) {
			return new ProcessResult<>(false, null);
		}
		if (result instanceof WebView) {
			return new ProcessResult<>(false, (WebView) result);
		}
		return new ProcessResult<>(false, new TextView(JSON.toJSONString(result)));
	}
	
	private static class RemoteMethod {
		private final RemoteClass remoteClass;
		private final Method method;
		private final String[] params;
		private final Parameter[] parameters;
		public RemoteMethod(RemoteClass remoteClass, Method method, MethodParameterName mpn) throws NotFoundException {
			this.remoteClass = remoteClass;
			this.method = method;
			params = mpn.getParameterNameByMethod(method);
			parameters = method.getParameters();
		}
		public RemoteClass getRemoteClass() {
			return remoteClass;
		}
		public Method getMethod() {
			return method;
		}
		public String[] getParams() {
			return params;
		}
		public Parameter[] getParameters() {
			return parameters;
		}
	}

	private static class RemoteClass {
		private final boolean filter;
		private final String serverName;
		private final Class<?> clazz;
		public RemoteClass(Class<?> clazz) {
			this.clazz = clazz;
			filter = clazz.getAnnotation(Remote.class).filter();
			serverName = clazz.getAnnotation(Remote.class).serverName();
		}
		public boolean isFilter() {
			return filter;
		}
		public String getServerName() {
			return serverName;
		}
		public Class<?> getClazz() {
			return clazz;
		}
	}

}

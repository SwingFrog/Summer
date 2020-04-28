package com.swingfrog.summer.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.util.JSONConvertUtil;
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
	
	private static Logger log = LoggerFactory.getLogger(RemoteDispatchMgr.class);
	private Map<String, RemoteClass> remoteClassMap;

	private static class SingleCase {
		public static final RemoteDispatchMgr INSTANCE = new RemoteDispatchMgr();
	}
	
	private RemoteDispatchMgr() {
		remoteClassMap = new HashMap<>();
	}
	
	public static RemoteDispatchMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init() throws NotFoundException {
		Iterator<Class<?>> ite = ContainerMgr.get().iteratorRemoteList();
		while (ite.hasNext()) {
			Class<?> clazz = ite.next();
			log.info("server register remote {}", clazz.getSimpleName());
			remoteClassMap.put(clazz.getSimpleName(), new RemoteClass(clazz));
		}
	}
	
	public Method getMethod(SessionRequest req) {
		String remote = req.getRemote();
		String method = req.getMethod();
		RemoteClass remoteClass = remoteClassMap.get(remote);
		if (remoteClass != null) {
			RemoteMethod remoteMethod = remoteClass.getRemoteMethod(method);
			if (remoteMethod != null) {
				return remoteMethod.getMethod();
			}
		}
		return null;
	}
	
	private Object invoke(ServerContext serverContext, String remote, String method, JSONObject data, Map<Class<?>, Object> autoObj, Map<String, Object> autoNameObj) throws Throwable {
		RemoteClass remoteClass = remoteClassMap.get(remote);
		if (remoteClass != null) {
			if (remoteClass.filter && !remoteClass.serverName.equals(serverContext.getConfig().getServerName())) {
				throw new CodeException(SessionException.REMOTE_WAS_PROTECTED);
			}
			RemoteMethod remoteMethod = remoteClass.getRemoteMethod(method);
			if (remoteMethod != null) {
				Object remoteObj = ContainerMgr.get().getDeclaredComponent(remoteClass.getClazz());
				Method remoteMod = remoteMethod.getMethod();
				String[] params = remoteMethod.getParams();
				Type[] paramTypes = remoteMethod.paramTypes();
				Parameter[] parameters = remoteMethod.getParameters();
				boolean auto = ContainerMgr.get().isAutowiredParameter(remoteClass.getClazz());
				Object[] obj = new Object[params.length];
				try {
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
									if (autoObj != null && autoObj.containsKey(type)) {
										obj[i] = autoObj.get(type);
									} else if (autoNameObj != null && autoNameObj.containsKey(param)) {
										obj[i] = autoNameObj.get(param);
									} else {
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
			} else {
				throw new CodeException(SessionException.METHOD_NOT_EXIST);
			}
		} else {
			throw new CodeException(SessionException.REMOTE_NOT_EXIST);
		}
	}
	
	public ProcessResult<SessionResponse> process(ServerContext serverContext, SessionRequest req, SessionContext sctx) throws Throwable {
		String remote = req.getRemote();
		String method = req.getMethod();
		JSONObject data = req.getData();
		Map<Class<?>, Object> autoObj = new HashMap<>();
		autoObj.put(SessionContext.class, sctx);
		autoObj.put(SessionRequest.class, req);
		Object result = invoke(serverContext, remote, method, data, autoObj, null);
		if (result instanceof AsyncResponse) {
			return new ProcessResult<>(true, null);
		}
		return new ProcessResult<>(false, SessionResponse.buildMsg(req, result));
	}
	
	public ProcessResult<WebView> webProcess(ServerContext serverContext, WebRequest req, SessionContext sctx) throws Throwable {
		String remote = req.getRemote();
		String method = req.getMethod();
		JSONObject data = req.getData();
		Map<Class<?>, Object> autoObj = new HashMap<>();
		autoObj.put(SessionContext.class, sctx);
		autoObj.put(SessionRequest.class, req);
		Map<String, Object> autoNameObj = new HashMap<>();
		for (String key : req.getFileUploadMap().keySet()) {
			autoNameObj.put(key, req.getFileUploadMap().get(key));
		}
		Object result = invoke(serverContext, remote, method, data, autoObj, autoNameObj);
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
	
	private static class RemoteClass {
		private boolean filter;
		private String serverName;
		private Class<?> clazz;
		private Map<String, RemoteMethod> remoteMethodMap = new HashMap<>();
		public RemoteClass(Class<?> clazz) throws NotFoundException {
			this.clazz = clazz;
			this.filter = clazz.getAnnotation(Remote.class).filter();
			this.serverName = clazz.getAnnotation(Remote.class).serverName();
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				log.info("remote register {}.{}", clazz.getSimpleName(), method.getName());
				remoteMethodMap.put(method.getName(), new RemoteMethod(method, new MethodParameterName(clazz)));
			}
		}
		public Class<?> getClazz() {
			return clazz;
		}
		public RemoteMethod getRemoteMethod(String method) {
			return remoteMethodMap.get(method);
		}
	}
	
	private static class RemoteMethod {
		private Method method;
		private String[] params;
		private Type[] paramTypes;
		private Parameter[] parameters;
		public RemoteMethod(Method method, MethodParameterName mpn) throws NotFoundException {
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
}

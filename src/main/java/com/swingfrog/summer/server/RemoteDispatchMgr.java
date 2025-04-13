package com.swingfrog.summer.server;

import java.lang.reflect.*;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.swingfrog.summer.annotation.ParamPacking;
import com.swingfrog.summer.annotation.RequestMapping;
import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.exception.CodeMsg;
import com.swingfrog.summer.server.exception.RemoteRuntimeException;
import com.swingfrog.summer.server.handler.RemoteHandler;
import com.swingfrog.summer.struct.AutowireParam;
import com.swingfrog.summer.util.*;
import com.swingfrog.summer.web.WebMgr;
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
				if (method.getModifiers() != Modifier.PUBLIC
						|| ProtobufUtil.hasProtobufParam(method)
						|| MethodUtil.contains(RemoteHandler.class, method)) {
					continue;
				}
				RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
				String remoteMethod;
				if (requestMapping != null) {
					remoteMethod = requestMapping.value();
				} else {
					remoteMethod = RemoteUtil.mergeRemoteMethod(clazz.getSimpleName(), method.getName());
				}
				if (remoteMethodMap.putIfAbsent(remoteMethod, new RemoteMethod(remoteMethod, remoteClass, method, mpn)) == null) {
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

	public Method getMethod(SessionRequest req) {
		String remote = req.getRemote();
		String method = req.getMethod();
		RemoteMethod remoteMethod = remoteMethodMap.get(RemoteUtil.mergeRemoteMethod(remote, method));
		if (remoteMethod == null)
			return null;
		return remoteMethod.getMethod();
	}

	public boolean containsRemoteMethod(String remoteMethod) {
		return remoteMethodMap.containsKey(remoteMethod);
	}
	
	public Object invoke(ServerContext serverContext, SessionRequest req, SessionContext sctx,
						  String remote, String method, JSONObject data, AutowireParam autowireParam) throws Throwable {
		Map<Class<?>, Object> objForTypes = autowireParam.getTypes();
		Map<String, Object> objForNames = autowireParam.getNames();
		RemoteMethod remoteMethod = remoteMethodMap.get(RemoteUtil.mergeRemoteMethod(remote, method));
		if (remoteMethod == null) {
			throw new CodeException(SessionException.METHOD_NOT_EXIST);
		}
		RemoteClass remoteClass = remoteMethod.getRemoteClass();
		if (remoteClass.isFilter() && !remoteClass.getServerName().equals(serverContext.getConfig().getServerName())) {
			throw new CodeException(SessionException.REMOTE_WAS_PROTECTED);
		}
		Object remoteObj = ContainerMgr.get().getDeclaredComponent(remoteClass.getClazz());

		if (remoteObj instanceof RemoteHandler)
			((RemoteHandler) remoteObj).handleReady(sctx, req);

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
				Class<?> typeClazz = parameter.getType();
				Object value = null;
				if (auto) {
					if (objForTypes != null && objForTypes.containsKey(typeClazz)) {
						value = objForTypes.get(typeClazz);
					} else if (objForNames != null && objForNames.containsKey(param)) {
						value = objForNames.get(param);
					}
				}

				if (value == null) {
					boolean isBaseType = JSONConvertUtil.containsType(type);
					boolean isContainsKey = data.containsKey(param);
					if (isBaseType && isContainsKey) {
						value = JSONConvertUtil.convert(type, data, param);
					} else if (parameter.isAnnotationPresent(ParamPacking.class)) {
						value = processParamPacking(parameter.getType(), data);
					} else {
						if (isContainsKey) {
							try {
								value = JSON.parseObject(data.getString(param), type);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
						} else {
							if (auto && !isBaseType) {
								value = ContainerMgr.get().getComponent(typeClazz);
								if (value == null) {
									try {
										value = ((Class<?>) type).newInstance();
									} catch (Exception e) {
										log.error(e.getMessage(), e);
									}
								}
							}
						}
					}
				}

				if (value == null) {
					Optional optional = parameter.getAnnotation(Optional.class);
					if (optional != null) {
						value = ParamUtil.convert(type, optional.value());
					} else {
						CodeMsg codeMsg = SessionException.PARAMETER_ERROR;
						throw new CodeException(codeMsg.getCode(), String.format("%s, param [%s] not found", codeMsg.getMsg(), param));
					}
				}
				obj[i] = value;
			}
		} catch (CodeException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
		Object result = invoke(serverContext, req, sctx, remote, method, data, autowireParam);
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
		Object result = invoke(serverContext, req, sctx, remote, method, data, autowireParam);
		if (result instanceof AsyncResponse) {
			return new ProcessResult<>(true, null);
		}
		if (result == null) {
			return new ProcessResult<>(false, null);
		}
		if (result instanceof WebView) {
			return new ProcessResult<>(false, (WebView) result);
		}
		return new ProcessResult<>(false, WebMgr.get().getInteriorViewFactory().createDefaultView(result));
	}

	private Object processParamPacking(Class<?> clazz, JSONObject data) throws IllegalAccessException, InstantiationException {
		Object obj = clazz.newInstance();
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			Class<?> type = field.getType();
			String param = field.getName();
			Object fieldObj = null;
			if (data.containsKey(param)) {
				if (JSONConvertUtil.containsType(type)) {
					fieldObj = JSONConvertUtil.convert(type, data, param);
				} else {
					try {
						fieldObj = JSON.parseObject(data.getString(param), type);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
			if (fieldObj == null) {
				Optional optional = field.getAnnotation(Optional.class);
				if (optional != null) {
					fieldObj = ParamUtil.convert(type, optional.value());
				} else {
					CodeMsg codeMsg = SessionException.PARAMETER_ERROR;
					throw new CodeException(codeMsg.getCode(), String.format("%s, param [%s] not found", codeMsg.getMsg(), param));
				}
			}
			field.set(obj, fieldObj);
		}
		return obj;
	}

	public static class RemoteMethod {
		private final String api;
		private final RemoteClass remoteClass;
		private final Method method;
		private final String[] params;
		private final Parameter[] parameters;
		public RemoteMethod(String api, RemoteClass remoteClass, Method method, MethodParameterName mpn) throws NotFoundException {
			this.api = api;
			this.remoteClass = remoteClass;
			this.method = method;
			params = mpn.getParameterNameByMethod(method);
			parameters = method.getParameters();
		}
		public String getApi() {
			return api;
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

	public static class RemoteClass {
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

	public Map<String, RemoteMethod> getRemoteMethodMap() {
		return remoteMethodMap;
	}

}

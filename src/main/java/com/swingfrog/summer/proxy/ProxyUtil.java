package com.swingfrog.summer.proxy;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.swingfrog.summer.client.ClientMgr;
import com.swingfrog.summer.client.ClientRemote;
import com.swingfrog.summer.client.exception.CreateRemoteFailException;
import com.swingfrog.summer.concurrent.SynchronizedMgr;
import com.swingfrog.summer.db.DataBaseMgr;
import com.swingfrog.summer.ioc.ContainerMgr;
import com.swingfrog.summer.ioc.MethodParameterName;
import com.swingfrog.summer.redis.RedisMgr;

public class ProxyUtil {
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyService(T service) {
		Object obj = ProxyFactory.getProxyInstance(service, (obj1, method, args) -> {
			String synchronizedName = ContainerMgr.get().getSynchronizedName(method);
			String value = null;
			if (synchronizedName != null) {
				synchronizedName = String.join("-", "synchronized", synchronizedName);
				value = UUID.randomUUID().toString();
				SynchronizedMgr.get().lock(synchronizedName, value);
			}
			boolean transaction = false;
			if (ContainerMgr.get().isTransaction(method) && DataBaseMgr.get().notOpenTransaction()) {
				DataBaseMgr.get().openTransaction();
				transaction = true;
			}
			try {
				DataBaseMgr.get().setOwner(method);
				RedisMgr.get().setOwner(method);
				Object res = method.invoke(obj1, args);
				if (transaction) {
					DataBaseMgr.get().getConnection().commit();
				}
				return res;
			} catch (InvocationTargetException e) {
				if (transaction) {
					DataBaseMgr.get().getConnection().rollback();
				}
				throw e.getTargetException();
			} catch (Exception e) {
				if (transaction) {
					DataBaseMgr.get().getConnection().rollback();
				}
				throw e;
			} finally {
				DataBaseMgr.get().discardConnection(method);
				RedisMgr.get().discardConnection(method);
				if (synchronizedName != null) {
					SynchronizedMgr.get().unlock(synchronizedName, value);
				}
			}
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyRemote(T remote) {
		Object obj = ProxyFactory.getProxyInstance(remote, (obj1, method, args) -> {
			String synchronizedName = ContainerMgr.get().getSynchronizedName(method);
			String value = null;
			if (synchronizedName != null) {
				synchronizedName = String.join("-", "synchronized", synchronizedName);
				value = UUID.randomUUID().toString();
				SynchronizedMgr.get().lock(synchronizedName, value);
			}
			boolean transaction = false;
			if (ContainerMgr.get().isTransaction(method) && DataBaseMgr.get().notOpenTransaction()) {
				DataBaseMgr.get().openTransaction();
				transaction = true;
			}
			try {
				DataBaseMgr.get().setOwner(method);
				RedisMgr.get().setOwner(method);
				Object res = method.invoke(obj1, args);
				if (transaction) {
					DataBaseMgr.get().getConnection().commit();
				}
				return res;
			} catch (InvocationTargetException e) {
				if (transaction) {
					DataBaseMgr.get().getConnection().rollback();
				}
				throw e.getTargetException();
			} catch (Exception e) {
				if (transaction) {
					DataBaseMgr.get().getConnection().rollback();
				}
				throw e;
			} finally {
				DataBaseMgr.get().discardConnection(method);
				RedisMgr.get().discardConnection(method);
				if (synchronizedName != null) {
					SynchronizedMgr.get().unlock(synchronizedName, value);
				}
			}
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyClientRemote(T remote, String cluster, String name) {
		Object obj = ProxyFactory.getProxyInstance(remote, (obj1, method, args) -> {
			Map<String, Object> map = Maps.newHashMap();
			MethodParameterName mpn = new MethodParameterName(obj1.getClass());
			String[] params = mpn.getParameterNameByMethod(method);
			for (int i = 0; i < params.length; i ++) {
				map.put(params[i], args[i]);
			}
			ClientRemote clientRemote = ClientMgr.get().getClientRemote(cluster, name);
			if (clientRemote == null) {
				throw new CreateRemoteFailException("clientRemote is null");
			}
			return clientRemote.syncRemote(obj1.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyClientRemoteWithRetry(T remote, String cluster, String name) {
		Object obj = ProxyFactory.getProxyInstance(remote, (obj1, method, args) -> {
			Map<String, Object> map = Maps.newHashMap();
			MethodParameterName mpn = new MethodParameterName(obj1.getClass());
			String[] params = mpn.getParameterNameByMethod(method);
			for (int i = 0; i < params.length; i ++) {
				map.put(params[i], args[i]);
			}
			ClientRemote clientRemote = ClientMgr.get().getClientRemote(cluster, name);
			if (clientRemote == null) {
				throw new CreateRemoteFailException("clientRemote is null");
			}
			return clientRemote.rsyncRemote(obj1.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyRandomClientRemote(T remote, String cluster) {
		Object obj = ProxyFactory.getProxyInstance(remote, (obj1, method, args) -> {
			Map<String, Object> map = Maps.newHashMap();
			MethodParameterName mpn = new MethodParameterName(obj1.getClass());
			String[] params = mpn.getParameterNameByMethod(method);
			for (int i = 0; i < params.length; i ++) {
				map.put(params[i], args[i]);
			}
			ClientRemote clientRemote = ClientMgr.get().getRandomClientRemote(cluster);
			if (clientRemote == null) {
				throw new CreateRemoteFailException("clientRemote is null");
			}
			return clientRemote.syncRemote(obj1.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
		});
		return (T)obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProxyRandomClientRemoteWithRetry(T remote, String cluster) {
		Object obj = ProxyFactory.getProxyInstance(remote, (obj1, method, args) -> {
			Map<String, Object> map = Maps.newHashMap();
			MethodParameterName mpn = new MethodParameterName(obj1.getClass());
			String[] params = mpn.getParameterNameByMethod(method);
			for (int i = 0; i < params.length; i ++) {
				map.put(params[i], args[i]);
			}
			ClientRemote clientRemote = ClientMgr.get().getRandomClientRemote(cluster);
			if (clientRemote == null) {
				throw new CreateRemoteFailException("clientRemote is null");
			}
			return clientRemote.rsyncRemote(obj1.getClass().getSimpleName(), method.getName(), map, method.getGenericReturnType());
		});
		return (T)obj;
	}
	
}

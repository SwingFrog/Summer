package com.swingfrog.summer.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Maps;
import com.swingfrog.summer.config.ClientGroupConfig;
import com.swingfrog.summer.util.ThreadCountUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.client.exception.CreateRemoteFailException;
import com.swingfrog.summer.config.ClientConfig;
import com.swingfrog.summer.config.ConfigMgr;
import com.swingfrog.summer.proxy.ProxyUtil;

import javassist.NotFoundException;

public class ClientMgr {

	private static final Logger log = LoggerFactory.getLogger(ClientMgr.class);
	private final Map<String, ClientCluster> nameToCluster;
	private final ConcurrentMap<Class<?>, Object> remoteMap;
	private final AtomicLong currentId = new AtomicLong(0);
	private final ScheduledExecutorService executor;
	private final ConcurrentMap<Long, ScheduledFuture<?>> futureMap;

	private EventLoopGroup workerGroup;
	private ExecutorService[] eventExecutors;
	
	private static class SingleCase {
		public static final ClientMgr INSTANCE = new ClientMgr();
	}
	
	private ClientMgr() {
		nameToCluster = Maps.newHashMap();
		remoteMap = Maps.newConcurrentMap();
		executor = Executors.newScheduledThreadPool(1, new DefaultThreadFactory("ClientAsyncRemoteCheck"));
		futureMap = Maps.newConcurrentMap();
	}
	
	public static ClientMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void init() throws NotFoundException {
		log.info("client init...");
		ClientGroupConfig clientGroupConfig = ConfigMgr.get().getClientGroupConfig();
		log.info("client workerThread {}", clientGroupConfig.getWorkerThread());
		log.info("client eventThread {}", clientGroupConfig.getEventThread());
		ClientConfig[] configs = ConfigMgr.get().getClientConfigs();
		if (configs != null && configs.length > 0) {
			workerGroup = new NioEventLoopGroup(clientGroupConfig.getWorkerThread(),
					new DefaultThreadFactory("ClientWorker"));
			int count = ThreadCountUtil.cpuDenseness(clientGroupConfig.getEventThread());
			eventExecutors = new ExecutorService[count];
			for (int i = 0; i < count; i++) {
				eventExecutors[i] = Executors.newSingleThreadExecutor(new DefaultThreadFactory("ClientEvent_" + i));
			}
			log.info("client count {}", configs.length);
			for (ClientConfig config : configs) {
				ClientCluster clientCluster = nameToCluster.get(config.getCluster());
				if (clientCluster == null) {
					log.info("client create cluster {}", config.getCluster());
					clientCluster = new ClientCluster();
					nameToCluster.put(config.getCluster(), clientCluster);
				}
				ClientGroup clientGroup = new ClientGroup();
				for (int j = 0; j < config.getConnectNum(); j++) {
					clientGroup.addClient(new Client(j, config, workerGroup));
				}
				clientCluster.addClient(config.getServerName(), clientGroup);
			}
			PushDispatchMgr.get().init();
		} else {
			log.warn("no clients");
		}
	}
	
	public void connectAll() {
		if (nameToCluster.size() > 0) {
			log.info("clients connect...");
			for (ClientCluster clientCluster : nameToCluster.values()) {
				List<Client> clients = clientCluster.listClients();
				for (Client client : clients) {
					client.connect();
				}
			}
		}
	}

	public void shutdown() {
		if (nameToCluster.size() > 0) {
			log.info("clients shutdown...");
			for (ClientCluster clientCluster : nameToCluster.values()) {
				List<Client> clients = clientCluster.listClients();
				for (Client client : clients) {
					client.shutdown();
				}
			}
		}
		try {
			workerGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void shutdownEvent() {
		for (ExecutorService eventExecutor : eventExecutors) {
			eventExecutor.shutdown();
			try {
				while (!eventExecutor.isTerminated()) {
					eventExecutor.awaitTermination(1, TimeUnit.SECONDS);
				}
			} catch (InterruptedException e){
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public ClientRemote getClientRemote(String cluster, String name) {
		ClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster != null) {
			Client client = clientCluster.getClientByName(name);
			if (client != null) {
				return client.getClientRemote();
			}
		}
		return null;
	}

	public ClientRemote getRandomClientRemote(String cluster) {
		ClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster != null) {
			Client client = clientCluster.getClientWithNext();
			if (client != null) {
				return client.getClientRemote();
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRemoteInvokeObject(String cluster, String name, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyClientRemote(clazz.newInstance(), cluster, name);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRemoteInvokeObjectWithRetry(String cluster, String name, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyClientRemoteWithRetry(clazz.newInstance(), cluster, name);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRandomRemoteInvokeObject(String cluster, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyRandomClientRemote(clazz.newInstance(), cluster);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getRandomRemoteInvokeObjectWithRetry(String cluster, Class<?> clazz) {
		Object obj = remoteMap.get(clazz);
		if (obj == null) {
			try {
				obj = ProxyUtil.getProxyRandomClientRemoteWithRetry(clazz.newInstance(), cluster);
			} catch (Exception e) {
				throw new CreateRemoteFailException("remote object newInstance fail");
			}
			remoteMap.put(clazz, obj);
		}
		return (T) obj;
	}
	
	long incrementCurrentId() {
		return currentId.incrementAndGet();
	}

	ScheduledExecutorService getAsyncRemoteCheckExecutor() {
		return executor;
	}

	ConcurrentMap<Long, ScheduledFuture<?>> getFutureMap() {
		return futureMap;
	}

	Executor getEventExecutor(int clientId) {
		return eventExecutors[clientId % eventExecutors.length];
	}

}

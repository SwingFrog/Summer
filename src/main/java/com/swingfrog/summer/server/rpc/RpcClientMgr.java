package com.swingfrog.summer.server.rpc;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.swingfrog.summer.server.SessionContext;

public class RpcClientMgr {
	
	private static final Logger log = LoggerFactory.getLogger(RpcClientMgr.class);
	
	private final ConcurrentMap<String, RpcClientCluster> nameToCluster;
	
	private static class SingleCase {
		public static final RpcClientMgr INSTANCE = new RpcClientMgr();
	}
	
	private RpcClientMgr() {
		nameToCluster = new ConcurrentHashMap<>();
	}
	
	public static RpcClientMgr get() {
		return SingleCase.INSTANCE;
	}
	
	public void add(SessionContext sctx, String cluster, String serverName) {
		log.debug("server register rpc client cluster[{}] serverName[{}] sessionContext[{}]", cluster, serverName, sctx);
		RpcClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster == null) {
			clientCluster = new RpcClientCluster();
			clientCluster.addClient(serverName, new RpcClientGroup());
			nameToCluster.put(cluster, clientCluster);
		}
		clientCluster.getRpcClientGroup(serverName).addClient(sctx);
	}
	
	public void remove(SessionContext sctx) {
		for (RpcClientCluster rpcClientCluster : nameToCluster.values()) {
			rpcClientCluster.removeClient(sctx);
		}
	}
	
	public SessionContext getClientSessionContext(String cluster, String name) {
		RpcClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster != null) {
			return clientCluster.getClientByName(name);
		}
		return null;
	}
	
	public SessionContext getRandomClientSessionContext(String cluster) {
		RpcClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster != null) {
			return clientCluster.getClientWithNext();
		}
		return null;
	}
	
	public List<SessionContext> getAllClientSessionContext(String cluster) {
		RpcClientCluster clientCluster = nameToCluster.get(cluster);
		if (clientCluster != null) {
			return clientCluster.listOneClients();
		}
		return null;
	}
}

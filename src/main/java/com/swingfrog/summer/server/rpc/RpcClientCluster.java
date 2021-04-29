package com.swingfrog.summer.server.rpc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.util.PollingUtil;

public class RpcClientCluster {

	private final AtomicInteger next;
	private final List<RpcClientGroup> clientGroupList;
	private final Map<String, RpcClientGroup> nameToClientGroup;
	
	public RpcClientCluster() {
		next = new AtomicInteger();
		clientGroupList = Lists.newArrayList();
		nameToClientGroup = Maps.newHashMap();
	}
	
	public void addClient(String name, RpcClientGroup clientGroup) {
		clientGroupList.add(clientGroup);
		nameToClientGroup.put(name, clientGroup);
	}
	
	public void removeClient(SessionContext sctx) {
		for (RpcClientGroup rpcClientGroup : clientGroupList) {
			rpcClientGroup.removeClient(sctx);
		}
	}
	
	public RpcClientGroup getRpcClientGroup(String name) {
		return nameToClientGroup.get(name);
	}
	
	public SessionContext getClientByName(String name) {
		return nameToClientGroup.get(name).getClientWithNext();
	}
	
	public SessionContext getClientWithNext() {
		RpcClientGroup rpcClientGroup = PollingUtil.getNext(next, clientGroupList, RpcClientGroup::hasAnyActive);
		if (rpcClientGroup == null) {
			return null;
		}
		return rpcClientGroup.getClientWithNext();
	}
	
	public List<SessionContext> listAllClients() {
		List<SessionContext> list = Lists.newArrayList();
		for (RpcClientGroup rpcClientGroup : clientGroupList) {
			list.addAll(rpcClientGroup.listClients());
		}
		return list;
	}
	
	public List<SessionContext> listOneClients() {
		List<SessionContext> list = Lists.newArrayList();
		for (RpcClientGroup rpcClientGroup : clientGroupList) {
			list.add(rpcClientGroup.getClientWithNext());
		}
		return list;
	}
	
}

package com.swingfrog.summer.server.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swingfrog.summer.server.SessionContext;

public class RpcClientCluster {

	private int next = -1;
	private final List<RpcClientGroup> clientGroupList;
	private final Map<String, RpcClientGroup> nameToClientGroup;
	
	public RpcClientCluster() {
		clientGroupList = new ArrayList<>();
		nameToClientGroup = new HashMap<>();
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
		int size = clientGroupList.size();
		if (size > 0) {
			if (size == 1) {
				return clientGroupList.get(0).getClientWithNext();
			}
			next ++;
			next = next % size;
			return clientGroupList.get(next % size).getClientWithNext();
		}
		return null;
	}
	
	public List<SessionContext> listAllClients() {
		List<SessionContext> list = new ArrayList<>();
		for (RpcClientGroup rpcClientGroup : clientGroupList) {
			list.addAll(rpcClientGroup.listClients());
		}
		return list;
	}
	
	public List<SessionContext> listOneClients() {
		List<SessionContext> list = new ArrayList<>();
		for (RpcClientGroup rpcClientGroup : clientGroupList) {
			list.add(rpcClientGroup.getClientWithNext());
		}
		return list;
	}
	
}

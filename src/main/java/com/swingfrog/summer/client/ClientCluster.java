package com.swingfrog.summer.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swingfrog.summer.server.rpc.RpcClientGroup;
import com.swingfrog.summer.util.PollingUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientCluster {

	private final AtomicInteger next;
	private final List<ClientGroup> clientGroupList;
	private final Map<String, ClientGroup> nameToClientGroup;
	
	public ClientCluster() {
		next = new AtomicInteger();
		clientGroupList = Lists.newArrayList();
		nameToClientGroup = Maps.newHashMap();
	}
	
	public void addClient(String name, ClientGroup clientGroup) {
		clientGroupList.add(clientGroup);
		nameToClientGroup.put(name, clientGroup);
	}
	
	public Client getClientByName(String name) {
		return nameToClientGroup.get(name).getClientWithNext();
	}
	
	public Client getClientWithNext() {
		ClientGroup clientGroup = PollingUtil.getNext(next, clientGroupList, ClientGroup::hasAnyActive);
		if (clientGroup == null) {
			return null;
		}
		return clientGroup.getClientWithNext();
	}
	
	public List<Client> listClients() {
		List<Client> list = Lists.newArrayList();
		for (ClientGroup clientGroup : clientGroupList) {
			list.addAll(clientGroup.listClients());
		}
		return list;
	}
	
}

package com.swingfrog.summer.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
		int size = clientGroupList.size();
		if (size == 0) {
			return null;
		}
		if (size == 1) {
			return clientGroupList.get(0).getClientWithNext();
		}
		int n = next.getAndIncrement();
		n = Math.abs(n);
		n = n % size;
		return clientGroupList.get(n).getClientWithNext();
	}
	
	public List<Client> listClients() {
		List<Client> list = Lists.newArrayList();
		for (ClientGroup clientGroup : clientGroupList) {
			list.addAll(clientGroup.listClients());
		}
		return list;
	}
	
}

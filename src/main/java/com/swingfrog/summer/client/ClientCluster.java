package com.swingfrog.summer.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class ClientCluster {

	private int next = -1;
	private List<ClientGroup> clientGroupList;
	private Map<String, ClientGroup> nameToClientGroup;
	
	public ClientCluster() {
		clientGroupList = Lists.newLinkedList();
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
	
	public List<Client> listClients() {
		List<Client> list = Lists.newLinkedList();
		for (ClientGroup clientGroup : clientGroupList) {
			list.addAll(clientGroup.listClients());
		}
		return list;
	}
	
}

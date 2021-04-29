package com.swingfrog.summer.client;

import com.google.common.collect.Lists;
import com.swingfrog.summer.util.PollingUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientGroup {

	private final AtomicInteger next;
	private final List<Client> clientList;
	
	public ClientGroup() {
		next = new AtomicInteger();
		clientList = Lists.newArrayList();
	}
	
	public void addClient(Client client) {
		clientList.add(client);
	}
	
	public Client getClientWithNext() {
		return PollingUtil.getNext(next, clientList, client -> true);
	}
	
	public List<Client> listClients() {
		return clientList;
	}

	public boolean hasAnyActive() {
		for (Client client : clientList) {
			if (client.isActive() && client.getClientContext().isChannelActive())
				return true;
		}
		return true;
	}

}

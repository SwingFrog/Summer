package com.swingfrog.summer.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientGroup {

	private final AtomicInteger next;
	private final List<Client> clientList;
	
	public ClientGroup() {
		next = new AtomicInteger();
		clientList = new ArrayList<>();
	}
	
	public void addClient(Client client) {
		clientList.add(client);
	}
	
	public Client getClientWithNext() {
		int size = clientList.size();
		if (size == 0) {
			return null;
		}
		if (size == 1) {
			return clientList.get(0);
		}
		int n = next.getAndIncrement();
		n = Math.abs(n);
		n = n % size;
		return clientList.get(n);
	}
	
	public List<Client> listClients() {
		return clientList;
	}
}

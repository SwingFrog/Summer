package com.swingfrog.summer.client;

import java.util.ArrayList;
import java.util.List;

public class ClientGroup {

	private int next = -1;
	private List<Client> clientList;
	
	public ClientGroup() {
		clientList = new ArrayList<>();
	}
	
	public void addClient(Client client) {
		clientList.add(client);
	}
	
	public Client getClientWithNext() {
		int size = clientList.size();
		if (size > 0) {
			if (size == 1) {
				return clientList.get(0);
			}
			next ++;
			next = next % size;
			return clientList.get(next % size);
		}
		return null;
	}
	
	public List<Client> listClients() {
		return clientList;
	}
}

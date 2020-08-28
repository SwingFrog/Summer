package com.swingfrog.summer.server.rpc;

import java.util.ArrayList;
import java.util.List;

import com.swingfrog.summer.server.SessionContext;

public class RpcClientGroup {

	private int next = -1;
	private final List<SessionContext> clientList;
	
	public RpcClientGroup() {
		clientList = new ArrayList<>();
	}
	
	public void addClient(SessionContext client) {
		clientList.add(client);
	}
	
	public SessionContext getClientWithNext() {
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
	
	public List<SessionContext> listClients() {
		return clientList;
	}
	
	public void removeClient(SessionContext sctx) {
		clientList.remove(sctx);
	}
	
}

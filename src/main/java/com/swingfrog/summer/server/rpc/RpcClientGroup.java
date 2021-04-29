package com.swingfrog.summer.server.rpc;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.util.PollingUtil;

public class RpcClientGroup {

	private final AtomicInteger next;
	private final List<SessionContext> clientList;
	
	public RpcClientGroup() {
		next = new AtomicInteger();
		clientList = Lists.newArrayList();
	}
	
	public void addClient(SessionContext client) {
		clientList.add(client);
	}
	
	public SessionContext getClientWithNext() {
		return PollingUtil.getNext(next, clientList, SessionContext::isActive);
	}
	
	public List<SessionContext> listClients() {
		return clientList;
	}
	
	public void removeClient(SessionContext sctx) {
		clientList.remove(sctx);
	}

	public boolean hasAnyActive() {
		for (SessionContext sessionContext : clientList) {
			if (sessionContext.isActive())
				return true;
		}
		return false;
	}
	
}

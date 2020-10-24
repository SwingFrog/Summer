package com.swingfrog.summer.server;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class SessionContextGroup {

	private static final AttributeKey<SessionContext> KEY_SESSION_CONTEXT = AttributeKey.valueOf(SessionContext.class.getSimpleName());

	private final Set<SessionContext> sessionContexts = Sets.newConcurrentHashSet();
	
	public void createSession(Channel channel) {
		String id = channel.id().asLongText();
		InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
		SessionContext sctx = new SessionContext(id);
		sctx.setDirectAddress(address.getHostString());
		sctx.setPort(address.getPort());
		sctx.setCurrentMsgId(0);
		sctx.setLastRecvTime(System.currentTimeMillis());
		sctx.setChannel(channel);
		channel.attr(KEY_SESSION_CONTEXT).set(sctx);
		sessionContexts.add(sctx);
	}
	
	public void destroySession(Channel channel) {
		sessionContexts.remove(getSessionByChannel(channel));
	}
	
	public SessionContext getSessionByChannel(Channel channel) {
		return channel.attr(KEY_SESSION_CONTEXT).get();
	}
	
	public Iterator<SessionContext> iteratorSession() {
		return sessionContexts.iterator();
	}
	
	public Iterator<Channel> iteratorChannel() {
		return sessionContexts.stream().map(SessionContext::getChannel).iterator();
	}

	public boolean contains(SessionContext sessionContext) {
		return sessionContexts.contains(sessionContext);
	}

}

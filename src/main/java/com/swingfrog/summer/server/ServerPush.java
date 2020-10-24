package com.swingfrog.summer.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.RespProtobufMgr;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.server.exception.NotFoundSessionContextException;
import com.swingfrog.summer.server.rpc.RpcClientMgr;

public class ServerPush {
	
	private static final Logger log = LoggerFactory.getLogger(ServerPush.class);

	private final ServerContext serverContext;

	public ServerPush(ServerContext serverContext) {
		this.serverContext = serverContext;
	}
	
	public void asyncPushToClusterAllServer(String cluster, String remote, String method, Object data) {
		List<SessionContext> sessionContexts = RpcClientMgr.get().getAllClientSessionContext(cluster);
		if (sessionContexts == null) {
			throw new NotFoundSessionContextException(cluster);
		}
		log.debug("server push to cluster[{}]", cluster);
		asyncPushToSessionContexts(sessionContexts, remote, method, data);
	}
	
	public void syncPushToClusterAllServer(String cluster, String remote, String method, Object data) {
		List<SessionContext> sessionContexts = RpcClientMgr.get().getAllClientSessionContext(cluster);
		if (sessionContexts == null) {
			throw new NotFoundSessionContextException(cluster);
		}
		log.debug("server push to cluster[{}]", cluster);
		syncPushToSessionContexts(sessionContexts, remote, method, data);
	}

	public void asyncPushToClusterRandomServer(String cluster, String remote, String method, Object data) {
		SessionContext sessionContext = RpcClientMgr.get().getRandomClientSessionContext(cluster);
		if (sessionContext == null) {
			throw new NotFoundSessionContextException(cluster);
		}
		log.debug("server push to cluster[{}]", cluster);
		asyncPushToSessionContext(sessionContext, remote, method, data);
	}
	
	public void syncPushToClusterRandomServer(String cluster, String remote, String method, Object data) {
		SessionContext sessionContext = RpcClientMgr.get().getRandomClientSessionContext(cluster);
		if (sessionContext == null) {
			throw new NotFoundSessionContextException(cluster);
		}
		log.debug("server push to cluster[{}]", cluster);
		syncPushToSessionContext(sessionContext, remote, method, data);
	}
	
	public void asyncPushToClusterThisServer(String cluster, String serverName, String remote, String method, Object data) {
		SessionContext sessionContext = RpcClientMgr.get().getClientSessionContext(cluster, serverName);
		if (sessionContext == null) {
			throw new NotFoundSessionContextException(cluster, serverName);
		}
		log.debug("server push to cluster[{}] serverName[{}]", cluster, serverName);
		asyncPushToSessionContext(sessionContext, remote, method, data);
	}
	
	public void syncPushToClusterThisServer(String cluster, String serverName, String remote, String method, Object data) {
		SessionContext sessionContext = RpcClientMgr.get().getClientSessionContext(cluster, serverName);
		if (sessionContext == null) {
			throw new NotFoundSessionContextException(cluster, serverName);
		}
		log.debug("server push to cluster[{}] serverName[{}]", cluster, serverName);
		syncPushToSessionContext(sessionContext, remote, method, data);
	}
	
	public void asyncPushToSessionContext(SessionContext sessionContext, String remote, String method, Object data) {
		String msg = SessionResponse.buildPush(remote, method, data).toJSONString();
		serverContext.getPushExecutor().execute(()->{
			log.debug("server push to {} {}", sessionContext, msg);
			Channel channel = sessionContext.getChannel();
			write(channel, sessionContext, msg);
		});
	}

	public void syncPushToSessionContext(SessionContext sessionContext, String remote, String method, Object data) {
		String msg = SessionResponse.buildPush(remote, method, data).toJSONString();
		log.debug("server push to {} {}", sessionContext, msg);
		Channel channel = sessionContext.getChannel();
		write(channel, sessionContext, msg);
	}
	
	public void asyncPushToSessionContexts(Collection<SessionContext> sessionContexts, String remote, String method, Object data) {
		String msg = SessionResponse.buildPush(remote, method, data).toJSONString();
		serverContext.getPushExecutor().execute(()->{
			log.debug("server push to {} {}", sessionContexts, msg);
			for (SessionContext sessionContext : sessionContexts) {
				Channel channel = sessionContext.getChannel();
				if (channel != null) {
					SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(channel);
					write(channel, sctx, msg);
				}
			}
		});
	}

	public void syncPushToSessionContexts(Collection<SessionContext> sessionContexts, String remote, String method, Object data) {
		String msg = SessionResponse.buildPush(remote, method, data).toJSONString();
		log.debug("server push to {} {}", sessionContexts, msg);
		for (SessionContext sessionContext : sessionContexts) {
			Channel channel = sessionContext.getChannel();
			if (channel != null) {
				SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(channel);
				write(channel, sctx, msg);
			}
		}
	}

	public void asyncPushToAll(String remote, String method, Object data) {
		SessionContextGroup group = serverContext.getSessionContextGroup();
		String msg = SessionResponse.buildPush(remote, method, data).toJSONString();
		serverContext.getPushExecutor().execute(()->{
			log.debug("server push to all {}", msg);
			Iterator<Channel> ite = group.iteratorChannel();
			while (ite.hasNext()) {
				Channel channel = ite.next();
				SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(channel);
				write(channel, sctx, msg);
			}
		});
	}

	public void syncPushToAll(String remote, String method, Object data) {
		SessionContextGroup group = serverContext.getSessionContextGroup();
		String msg = SessionResponse.buildPush(remote, method, data).toJSONString();
		log.debug("server push to all {}", msg);
		Iterator<Channel> ite = group.iteratorChannel();
		while (ite.hasNext()) {
			Channel channel = ite.next();
			SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(channel);
			write(channel, sctx, msg);
		}
	}

	// protobuf

	public void asyncPushToSessionContext(SessionContext sessionContext, Message response) {
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		serverContext.getPushExecutor().execute(()->{
			log.debug("server push to {} {}", sessionContext, response);
			Channel channel = sessionContext.getChannel();
			write(channel, sessionContext, protobuf);
		});
	}

	public void syncPushToSessionContext(SessionContext sessionContext, Message response) {
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		log.debug("server push to {} {}", sessionContext, response);
		Channel channel = sessionContext.getChannel();
		write(channel, sessionContext, protobuf);
	}

	public void asyncPushToSessionContexts(Collection<SessionContext> sessionContexts, Message response) {
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		SessionContextGroup group = serverContext.getSessionContextGroup();
		serverContext.getPushExecutor().execute(()->{
			log.debug("server push to {} {}", sessionContexts, response);
			for (SessionContext sessionContext : sessionContexts) {
				Channel channel = sessionContext.getChannel();
				if (channel != null) {
					SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(channel);
					write(channel, sctx, protobuf);
				}
			}
		});
	}

	public void syncPushToSessionContexts(Collection<SessionContext> sessionContexts, Message response) {
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		SessionContextGroup group = serverContext.getSessionContextGroup();
		log.debug("server push to {} {}", sessionContexts, response);
		for (SessionContext sessionContext : sessionContexts) {
			Channel channel = sessionContext.getChannel();
			if (channel != null) {
				SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(channel);
				write(channel, sctx, protobuf);
			}
		}
	}

	public void asyncPushToAll(Message response) {
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		SessionContextGroup group = serverContext.getSessionContextGroup();
		serverContext.getPushExecutor().execute(()->{
			log.debug("server push to all {}", response);
			Iterator<Channel> ite = group.iteratorChannel();
			while (ite.hasNext()) {
				Channel ctx = ite.next();
				SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
				write(ctx, sctx, protobuf);
			}
		});
	}

	public void syncPushToAll(Message response) {
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		SessionContextGroup group = serverContext.getSessionContextGroup();
		log.debug("server push to all {}", response);
		Iterator<Channel> ite = group.iteratorChannel();
		while (ite.hasNext()) {
			Channel ctx = ite.next();
			SessionContext sctx = serverContext.getSessionContextGroup().getSessionByChannel(ctx);
			write(ctx, sctx, protobuf);
		}
	}

	private void write(Channel channel, SessionContext sctx, String response) {
		if (channel == null) {
			return;
		}
		ServerWriteHelper.write(channel, serverContext, sctx, response);
	}

	private void write(Channel channel, SessionContext sctx, Protobuf protobuf) {
		if (channel == null) {
			return;
		}
		ServerWriteHelper.write(channel, serverContext, sctx, protobuf);
	}

}

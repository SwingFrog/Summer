package com.swingfrog.summer.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.RespProtobufMgr;
import com.swingfrog.summer.protocol.tiny.msg.TinyPush;
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
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		Object msg = buildPush(remote, method, data);
		pushExecutor.execute(() -> {
			log.debug("server push to {} {}", sessionContext, msg);
			write(sessionContext, msg);
		});
	}

	public void syncPushToSessionContext(SessionContext sessionContext, String remote, String method, Object data) {
		Object msg = buildPush(remote, method, data);
		log.debug("server push to {} {}", sessionContext, msg);
		write(sessionContext, msg);
	}
	
	public void asyncPushToSessionContexts(Collection<SessionContext> sessionContexts, String remote, String method, Object data) {
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		Object msg = buildPush(remote, method, data);
		pushExecutor.execute(() -> {
			log.debug("server push to {} {}", sessionContexts, msg);
			for (SessionContext sessionContext : sessionContexts) {
				write(sessionContext, msg);
			}
		});
	}

	public void syncPushToSessionContexts(Collection<SessionContext> sessionContexts, String remote, String method, Object data) {
		Object msg = buildPush(remote, method, data);
		log.debug("server push to {} {}", sessionContexts, msg);
		for (SessionContext sessionContext : sessionContexts) {
			write(sessionContext, msg);
		}
	}

	public void asyncPushToSessionContexts(Stream<SessionContext> sctxStream, String remote, String method, Object data) {
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		List<SessionContext> sessionContexts = sctxStream.collect(Collectors.toList());
		Object msg = buildPush(remote, method, data);
		pushExecutor.execute(() -> {
			log.debug("server push to part {}", msg);
			for (SessionContext sessionContext : sessionContexts) {
				write(sessionContext, msg);
			}
		});
	}

	public void syncPushToSessionContexts(Stream<SessionContext> sctxStream, String remote, String method, Object data) {
		Object msg = buildPush(remote, method, data);
		log.debug("server push to part {}", msg);
		sctxStream.forEach(sessionContext -> write(sessionContext, msg));
	}

	public void asyncPushToAll(String remote, String method, Object data) {
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		SessionContextGroup group = serverContext.getSessionContextGroup();
		Object msg = buildPush(remote, method, data);
		pushExecutor.execute(() -> {
			log.debug("server push to all {}", msg);
			Iterator<SessionContext> ite = group.iteratorSession();
			while (ite.hasNext()) {
				SessionContext sctx = ite.next();
				write(sctx, msg);
			}
		});
	}

	public void syncPushToAll(String remote, String method, Object data) {
		SessionContextGroup group = serverContext.getSessionContextGroup();
		Object msg = buildPush(remote, method, data);
		log.debug("server push to all {}", msg);
		Iterator<SessionContext> ite = group.iteratorSession();
		while (ite.hasNext()) {
			SessionContext sctx = ite.next();
			write(sctx, msg);
		}
	}

	private Object buildPush(String remote, String method, Object data) {
		if (serverContext.isTiny()) {
			if (data instanceof TinyPush) {
				return data;
			}
			short msgId = RemoteTinyDispatchMgr.get().getMsgId(remote, method);
			return TinyPush.ofJSON(msgId, data);
		}
		return SessionResponse.buildPush(remote, method, data).toJSONString();
	}

	// protobuf

	public void asyncPushToSessionContext(SessionContext sessionContext, Message response) {
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		pushExecutor.execute(() -> {
			log.debug("server push to {} {}", sessionContext, response);
			write(sessionContext, protobuf);
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
		write(sessionContext, protobuf);
	}

	public void asyncPushToSessionContexts(Collection<SessionContext> sessionContexts, Message response) {
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		pushExecutor.execute(() -> {
			log.debug("server push to {} {}", sessionContexts, response);
			for (SessionContext sessionContext : sessionContexts) {
				write(sessionContext, protobuf);
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
		log.debug("server push to {} {}", sessionContexts, response);
		for (SessionContext sessionContext : sessionContexts) {
			write(sessionContext, protobuf);
		}
	}

	public void asyncPushToSessionContexts(Stream<SessionContext> sctxStream, Message response) {
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		List<SessionContext> sessionContexts = sctxStream.collect(Collectors.toList());
		Protobuf protobuf = Protobuf.of(messageId, response);
		pushExecutor.execute(() -> {
			log.debug("server push to part {}", response);
			for (SessionContext sessionContext : sessionContexts) {
				write(sessionContext, protobuf);
			}
		});
	}

	public void syncPushToSessionContexts(Stream<SessionContext> sctxStream, Message response) {
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		log.debug("server push to part {}", response);
		sctxStream.forEach(sessionContext -> write(sessionContext, protobuf));
	}

	public void asyncPushToAll(Message response) {
		ExecutorService pushExecutor = serverContext.getPushExecutor();
		if (pushExecutor.isShutdown()) {
			log.debug("server push executor shutdown");
			return;
		}
		Integer messageId = RespProtobufMgr.get().getMessageId(response.getClass());
		if (messageId == null) {
			log.error("protobuf[{}] not found", response.getClass().getName());
			return;
		}
		Protobuf protobuf = Protobuf.of(messageId, response);
		SessionContextGroup group = serverContext.getSessionContextGroup();
		pushExecutor.execute(() -> {
			log.debug("server push to all {}", response);
			Iterator<SessionContext> ite = group.iteratorSession();
			while (ite.hasNext()) {
				SessionContext sctx = ite.next();
				write(sctx, protobuf);
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
		Iterator<SessionContext> ite = group.iteratorSession();
		while (ite.hasNext()) {
			SessionContext sctx = ite.next();
			write(sctx, protobuf);
		}
	}

	private void write(SessionContext sctx, Object response) {
		ServerWriteHelper.write(serverContext, sctx, response);
	}

	// simple push

	public void push(SessionContext sessionContext, String remote, String method, Object data) {
		syncPushToSessionContext(sessionContext, remote, method, data);
	}

	public void push(Collection<SessionContext> sessionContexts, String remote, String method, Object data) {
		syncPushToSessionContexts(sessionContexts, remote, method, data);
	}

	public void push(Stream<SessionContext> sctxStream, String remote, String method, Object data) {
		syncPushToSessionContexts(sctxStream, remote, method, data);
	}

	public void pushAll(String remote, String method, Object data) {
		syncPushToAll(remote, method, data);
	}

	public void push(SessionContext sessionContext, Message response) {
		syncPushToSessionContext(sessionContext, response);
	}

	public void push(Collection<SessionContext> sessionContexts, Message response) {
		syncPushToSessionContexts(sessionContexts, response);
	}

	public void push(Stream<SessionContext> sctxStream, Message response) {
		syncPushToSessionContexts(sctxStream, response);
	}

	public void pushAll(Message response) {
		syncPushToAll(response);
	}

	// async simple push

	public void asyncPush(SessionContext sessionContext, String remote, String method, Object data) {
		asyncPushToSessionContext(sessionContext, remote, method, data);
	}

	public void asyncPush(Collection<SessionContext> sessionContexts, String remote, String method, Object data) {
		asyncPushToSessionContexts(sessionContexts, remote, method, data);
	}

	public void asyncPush(Stream<SessionContext> sctxStream, String remote, String method, Object data) {
		asyncPushToSessionContexts(sctxStream, remote, method, data);
	}

	public void asyncPushAll(String remote, String method, Object data) {
		asyncPushToAll(remote, method, data);
	}

	public void asyncPush(SessionContext sessionContext, Message response) {
		asyncPushToSessionContext(sessionContext, response);
	}

	public void asyncPush(Collection<SessionContext> sessionContexts, Message response) {
		asyncPushToSessionContexts(sessionContexts, response);
	}

	public void asyncPush(Stream<SessionContext> sctxStream, Message response) {
		asyncPushToSessionContexts(sctxStream, response);
	}

	public void asyncPushAll(Message response) {
		asyncPushToAll(response);
	}

}

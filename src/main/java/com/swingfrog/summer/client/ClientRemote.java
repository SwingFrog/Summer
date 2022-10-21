package com.swingfrog.summer.client;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import com.swingfrog.summer.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swingfrog.summer.client.exception.SyncRemoteTimeOutException;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.server.exception.CodeException;

public class ClientRemote {
	
	private static final Logger log = LoggerFactory.getLogger(ClientRemote.class);
	private final ClientContext clientContext;
	
	public ClientRemote(ClientContext clientContext) {
		this.clientContext = clientContext;
	}
	
	public String getServerName() {
		return clientContext.getConfig().getServerName();
	}
	
	public void asyncRemote(String remote, String method, Object data, RemoteCallback remoteCallback) {
		if (remoteCallback == null) {
			throw new NullPointerException("remoteCallback is null");
		}
		SessionRequest sessionRequest = SessionRequest.buildRemote(ClientMgr.get().incrementCurrentId(), remote, method, data);
		PushDispatchMgr.get().putAsyncRemote(sessionRequest.getId(), remoteCallback);
		if (clientContext.getChannel() != null) {
			String msg = sessionRequest.toJSONString();
			log.debug("client request serverName[{}] async {}", clientContext.getConfig().getServerName(), msg);
			clientContext.getChannel().writeAndFlush(msg);
		} else {
			clientContext.getRequestQueue().add(sessionRequest);
		}
	}

	public void retryAsyncRemote(String remote, String method, Object data, RemoteCallback remoteCallback, long afterTimeRetry, TimeUnit unit) {
		if (remoteCallback == null) {
			throw new NullPointerException("remoteCallback is null");
		}
		SessionRequest sessionRequest = SessionRequest.buildRemote(ClientMgr.get().incrementCurrentId(), remote, method, data);
		String msg = sessionRequest.toJSONString();
		PushDispatchMgr.get().putAsyncRemote(sessionRequest.getId(), remoteCallback);
		if (clientContext.getChannel() != null) {
			log.debug("client request serverName[{}] retry async {}", clientContext.getConfig().getServerName(), msg);
			clientContext.getChannel().writeAndFlush(msg);
		}
		addAsyncRemoteTimeoutTask(sessionRequest.getId(), msg, afterTimeRetry, unit);
	}

	private void addAsyncRemoteTimeoutTask(long id, String msg, long afterTimeRetry, TimeUnit unit) {
		ClientMgr.get().getAsyncRemoteCheckExecutor().schedule(() -> {
			if (!PushDispatchMgr.get().containsAsyncRemote(id)) {
				ClientMgr.get().getFutureMap().remove(id);
				return;
			}
			if (clientContext.getChannel() != null) {
				log.debug("client request serverName[{}] retry async {}", clientContext.getConfig().getServerName(), msg);
				clientContext.getChannel().writeAndFlush(msg);
			}
			addAsyncRemoteTimeoutTask(id, msg, afterTimeRetry, unit);
		}, afterTimeRetry, unit);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T sendSyncRemote(long id, String msg, String remote, String method, Type type) {
		try {
			int count = 0;
			while (true) {
				if (clientContext.getChannel() != null) {
					log.debug("client request serverName[{}] sync {}", clientContext.getConfig().getServerName(), msg);
					clientContext.getChannel().writeAndFlush(msg);
					while (true) {
						SessionResponse sessionResponse = PushDispatchMgr.get().getAndRemoveSyncRemote(id);
						if (sessionResponse != null) {
							if (sessionResponse.getCode() != 0) {
								throw new CodeException(sessionResponse.getCode(), sessionResponse.getData().toString());
							}
							Object resp = sessionResponse.getData();
							if (resp != null) {
								String res = resp.toString();
								return (T) ParamUtil.convert(type, res);
							}
							return null;
						}
						Thread.sleep(1);
						count ++;
						if (count > clientContext.getConfig().getSyncRemoteTimeOutMs()) {
							throw new SyncRemoteTimeOutException(remote, method);
						}
					}
				}
				Thread.sleep(1);
				count ++;
				if (count > clientContext.getConfig().getSyncRemoteTimeOutMs()) {
					throw new SyncRemoteTimeOutException(remote, method);
				}
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public <T> T syncRemote(String remote, String method, Object data, Type type) {
		SessionRequest sessionRequest = SessionRequest.buildRemote(ClientMgr.get().incrementCurrentId(), remote, method, data);
		String msg = sessionRequest.toJSONString();
		try {
			return sendSyncRemote(sessionRequest.getId(), msg, remote, method, type);
		} catch (SyncRemoteTimeOutException e) {
			PushDispatchMgr.get().discardSyncRemote(sessionRequest.getId());
			throw e;
		}
	}

	public <T> T rsyncRemote(String remote, String method, Object data, Type type) {
		SessionRequest sessionRequest = SessionRequest.buildRemote(ClientMgr.get().incrementCurrentId(), remote, method, data);
		String msg = sessionRequest.toJSONString();
		for (;;) {
			try {				
				return sendSyncRemote(sessionRequest.getId(), msg, remote, method, type);
			} catch (SyncRemoteTimeOutException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
}

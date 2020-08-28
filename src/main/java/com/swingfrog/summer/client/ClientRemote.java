package com.swingfrog.summer.client;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
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
		SessionRequest sessionRequest = SessionRequest.buildRemote(
				ClientMgr.get().incrementCurrentId(), remote, method, data);
		PushDispatchMgr.get().putAsyncRemote(sessionRequest.getId(), remoteCallback);
		if (clientContext.getChannel() != null) {
			String msg = sessionRequest.toJSONString();
			log.debug("client request serverName[{}] async {}", clientContext.getConfig().getServerName(), msg);
			clientContext.getChannel().writeAndFlush(msg);
		} else {
			clientContext.getRequestQueue().add(sessionRequest);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T syncRemote(String remote, String method, Object data, Type type) {
		try {
			int count = 0;
			while (true) {
				if (clientContext.getChannel() != null) {
					SessionRequest sessionRequest = SessionRequest.buildRemote(
							ClientMgr.get().incrementCurrentId(), remote, method, data);
					String msg = sessionRequest.toJSONString();
					log.debug("client request serverName[{}] sync {}", clientContext.getConfig().getServerName(), msg);
					clientContext.getChannel().writeAndFlush(msg);
					while (true) {
						SessionResponse sessionResponse = PushDispatchMgr.get().getAndRemoveSyncRemote(sessionRequest.getId());
						if (sessionResponse != null) {
							if (sessionResponse.getCode() != 0) {
								throw new CodeException(sessionResponse.getCode(), sessionResponse.getData().toString());
							}
							Object resp = sessionResponse.getData();
							if (resp != null) {
								String res = resp.toString();
								if (type == boolean.class || type == Boolean.class) {
									return (T) Boolean.valueOf(res);
								} else if (type == byte.class || type == Byte.class) {
									return (T) ((Object) Byte.parseByte(res));
								} else if (type == short.class || type == Short.class) {
									return (T) ((Object) Short.parseShort(res));
								} else if (type == int.class || type == Integer.class) {
									return (T) ((Object) Integer.parseInt(res));
								} else if (type == long.class || type == Long.class) {
									return (T) ((Object) Long.parseLong(res));
								} else if (type == String.class) {
									return (T) res;
								} else {
									return (T) JSON.parseObject(res, type);
								}
							}
							return null;
						}
						Thread.sleep(1);
						count ++;
						if (count > clientContext.getConfig().getSyncRemoteTimeOutMs()) {
							PushDispatchMgr.get().discardSyncRemote(sessionRequest.getId());
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

	public <T> T rsyncRemote(String remote, String method, Object data, Type type) {
		while (true) {
			try {				
				return syncRemote(remote, method, data, type);
			} catch (SyncRemoteTimeOutException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
}

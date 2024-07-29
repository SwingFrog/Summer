package com.swingfrog.summer.client;

import com.swingfrog.summer.client.exception.SyncRemoteTimeOutException;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

public class GroupClientRemote implements ClientRemote {

    private static final Logger log = LoggerFactory.getLogger(GroupClientRemote.class);
    private final ClientContext defaultClientContext;
    private final ClientCluster clientCluster;

    public GroupClientRemote(ClientContext clientContext, ClientCluster clientCluster) {
        this.defaultClientContext = clientContext;
        this.clientCluster = clientCluster;
    }

    private ClientContext nextClientContext() {
        Client client = clientCluster.getClientWithNext();
        if (client != null) {
            return client.getClientContext();
        }
        return defaultClientContext;
    }

    @Override
    public void asyncRemote(String remote, String method, Object data, RemoteCallback remoteCallback) {
        if (remoteCallback == null) {
            throw new NullPointerException("remoteCallback is null");
        }
        ClientContext clientContext = nextClientContext();
        SessionRequest sessionRequest = SessionRequest.buildRemote(ClientMgr.get().incrementCurrentId(), remote, method, data);
        PushDispatchMgr.get().putAsyncRemote(sessionRequest.getId(), remoteCallback);
        if (clientContext.isChannelActive()) {
            String msg = sessionRequest.toJSONString();
            log.debug("client request serverName[{}] async {}", clientContext.getConfig().getServerName(), msg);
            clientContext.getChannel().writeAndFlush(msg);
        } else {
            clientContext.getRequestQueue().add(sessionRequest);
        }
    }

    @Override
    public void retryAsyncRemote(String remote, String method, Object data, RemoteCallback remoteCallback, long afterTimeRetry, TimeUnit unit) {
        if (remoteCallback == null) {
            throw new NullPointerException("remoteCallback is null");
        }
        ClientContext clientContext = nextClientContext();
        SessionRequest sessionRequest = SessionRequest.buildRemote(ClientMgr.get().incrementCurrentId(), remote, method, data);
        String msg = sessionRequest.toJSONString();
        PushDispatchMgr.get().putAsyncRemote(sessionRequest.getId(), remoteCallback);
        if (clientContext.isChannelActive()) {
            log.debug("client request serverName[{}] retry async {}", clientContext.getConfig().getServerName(), msg);
            clientContext.getChannel().writeAndFlush(msg);
        }
        addAsyncRemoteTimeoutTask(sessionRequest.getId(), msg, afterTimeRetry, unit);
    }

    @Override
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

    @Override
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

    private void addAsyncRemoteTimeoutTask(long id, String msg, long afterTimeRetry, TimeUnit unit) {
        ClientMgr.get().getAsyncRemoteCheckExecutor().schedule(() -> {
            if (!PushDispatchMgr.get().containsAsyncRemote(id)) {
                ClientMgr.get().getFutureMap().remove(id);
                return;
            }
            ClientContext clientContext = nextClientContext();
            if (clientContext.isChannelActive()) {
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
                ClientContext clindClientContext = nextClientContext();
                if (clindClientContext.isChannelActive()) {
                    log.debug("client request serverName[{}] sync {}", clindClientContext.getConfig().getServerName(), msg);
                    clindClientContext.getChannel().writeAndFlush(msg);
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
                        if (count > clindClientContext.getConfig().getSyncRemoteTimeOutMs()) {
                            throw new SyncRemoteTimeOutException(remote, method);
                        }
                    }
                }
                Thread.sleep(1);
                count ++;
                if (count > defaultClientContext.getConfig().getSyncRemoteTimeOutMs()) {
                    throw new SyncRemoteTimeOutException(remote, method);
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}

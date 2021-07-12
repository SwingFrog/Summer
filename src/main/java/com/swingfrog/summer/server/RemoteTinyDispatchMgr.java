package com.swingfrog.summer.server;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.tiny.msg.TinyResp;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.server.async.ProcessResult;
import com.swingfrog.summer.server.exception.RemoteRuntimeException;
import com.swingfrog.summer.struct.AutowireParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RemoteTinyDispatchMgr {

    private static final Logger log = LoggerFactory.getLogger(RemoteTinyDispatchMgr.class);

    private final Map<Integer, RemoteMethod> idToRemotes = Maps.newHashMap();
    private final Table<String, String, Integer> remoteToIds = HashBasedTable.create();

    private static class SingleCase {
        public static final RemoteTinyDispatchMgr INSTANCE = new RemoteTinyDispatchMgr();
    }

    private RemoteTinyDispatchMgr() {
    }

    public static RemoteTinyDispatchMgr get() {
        return RemoteTinyDispatchMgr.SingleCase.INSTANCE;
    }

    public void addRemote(int id, String remote, String method) {
        RemoteMethod oldRemote = idToRemotes.putIfAbsent(id, new RemoteMethod(remote, method));
        if (oldRemote != null) {
            throw new RemoteRuntimeException("RemoteTiny id[?] remote[?] duplication", id, remote);
        }
        remoteToIds.put(remote, method, id);
        log.info("tiny remote {}.{} bind msgId[{}]", remote, method, id);
    }

    public RemoteMethod getRemote(int id) {
        return idToRemotes.get(id);
    }

    public int getMsgId(String remote, String method) {
        Integer id = remoteToIds.get(remote, method);
        if (id == null) {
            throw new RemoteRuntimeException("RemoteTiny remote[?] method[?] not found id", remote, method);
        }
        return id;
    }

    public ProcessResult<TinyResp> process(ServerContext serverContext, SessionRequest req, SessionContext sctx,
                                       AutowireParam autowireParam) throws Throwable {
        String remote = req.getRemote();
        String method = req.getMethod();
        JSONObject data = req.getData();
        Map<Class<?>, Object> objForTypes = autowireParam.getTypes();
        objForTypes.putIfAbsent(SessionContext.class, sctx);
        objForTypes.putIfAbsent(SessionRequest.class, req);
        Object result = RemoteDispatchMgr.get().invoke(serverContext, req, sctx, remote, method, data, autowireParam);
        if (result instanceof AsyncResponse) {
            return new ProcessResult<>(true, null);
        }
        if (result instanceof TinyResp) {
            return new ProcessResult<>(false, (TinyResp) result);
        }
        return new ProcessResult<>(false, TinyResp.ofJSON(result));
    }

    public static class RemoteMethod {
        private final String remote;
        private final String method;

        public RemoteMethod(String remote, String method) {
            this.remote = remote;
            this.method = method;
        }

        public String getRemote() {
            return remote;
        }

        public String getMethod() {
            return method;
        }
    }

}

package com.swingfrog.summer.meter.string;

import com.alibaba.fastjson.JSON;
import com.swingfrog.summer.meter.MeterClient;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.protocol.SessionResponse;
import io.netty.channel.Channel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractMeterStringClient implements MeterClient {

    protected final int id;
    private Channel channel;

    private final AtomicInteger sendReqId = new AtomicInteger();
    private long waitRespId;
    private Callback<?> waitCallback;
    private final Queue<WaitSendReq> waitSendReqQueue = new ConcurrentLinkedQueue<>();

    private final ConcurrentMap<String, Push<?>> pushMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Queue<Push<?>>> oncePushMap = new ConcurrentHashMap<>();

    protected AbstractMeterStringClient(int id) {
        this.id = id;
    }

    @Override
    public void sendHeartBeat() {
        write(ProtocolConst.PING);
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    void recv(String msg) {
        if (ProtocolConst.PONG.equals(msg)) {
            return;
        }
        try {
            SessionResponse response = JSON.parseObject(msg, SessionResponse.class);
            serverTime(response.getTime());
            if (response.getId() != 0) {
                if (response.getId() == waitRespId) {
                    if (response.getCode() == 0) {
                        Object data = parseData(response.getData(), parseClass(waitCallback));
                        waitCallback.accept(data);
                    } else {
                        waitCallback.failure(response.getCode(), response.getData().toString());
                    }
                    nextReq();
                }
            } else {
                Object data = null;
                String pushName = getPushName(response.getRemote(), response.getMethod());
                Push<?> push = pushMap.get(pushName);
                if (push != null) {
                    data = parseData(response.getData(), parseClass(push));
                    push.accept(data);
                }
                Queue<Push<?>> pushQueue = oncePushMap.remove(pushName);
                if (pushQueue != null) {
                    push = pushQueue.poll();
                    while (push != null) {
                        if (data == null) {
                            data = parseData(response.getData(), parseClass(push));
                        }
                        push.accept(data);
                        push = pushQueue.poll();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void req(String remote, String method, Object data, Callback<?> callback) {
        SessionRequest request = SessionRequest.buildRemote(sendReqId.incrementAndGet(), remote, method, data);
        if (waitSendReqQueue.isEmpty()) {
            sendReq(request, callback);
            return;
        }
        waitSendReqQueue.add(new WaitSendReq(request, callback));
    }

    private void nextReq() {
        WaitSendReq waitSendReq = waitSendReqQueue.poll();
        if (waitSendReq == null)
            return;
        sendReq(waitSendReq.request, waitSendReq.callback);
    }

    private void sendReq(SessionRequest request, Callback<?> callback) {
        waitRespId = request.getId();
        waitCallback = callback;
        write(request.toJSONString());
    }

    protected void registerPush(String remote, String method, Push<?> push) {
        pushMap.putIfAbsent(getPushName(remote, method), push);
    }

    protected void oncePush(String remote, String method, Push<?> push) {
        oncePushMap.computeIfAbsent(getPushName(remote, method), (k) -> new ConcurrentLinkedQueue<>()).add(push);
    }

    private String getPushName(String remote, String method) {
        return String.format("%s.%s", remote, method).intern();
    }

    private Object parseData(Object resp, Type type) {
        if (resp != null) {
            String res = resp.toString();
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.valueOf(res);
            } else if (type == byte.class || type == Byte.class) {
                return Byte.parseByte(res);
            } else if (type == short.class || type == Short.class) {
                return Short.parseShort(res);
            } else if (type == int.class || type == Integer.class) {
                return Integer.parseInt(res);
            } else if (type == long.class || type == Long.class) {
                return Long.parseLong(res);
            } else if (type == String.class) {
                return res;
            } else {
                return JSON.parseObject(res, type);
            }
        }
        return null;
    }

    private Type parseClass(Object object) {
        Class<?> clazz = object.getClass();
        Type superClass = clazz.getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs != null && typeArgs.length > 0) {
                return typeArgs[0];
            }
        }
        return null;
    }

    void write(String req) {
        if (!isActive())
            return;
        channel.writeAndFlush(req);
    }

    void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected static class WaitSendReq {
        private final SessionRequest request;
        private final Callback<?> callback;
        public WaitSendReq(SessionRequest request, Callback<?> callback) {
            this.request = request;
            this.callback = callback;
        }
    }

    protected static abstract class Callback<T> {
        @SuppressWarnings("unchecked")
        private void accept(Object object) {
            success((T) object);
        }
        protected abstract void success(T resp);
        protected abstract void failure(long code, String msg);
    }

    protected static abstract class Push<T> {
        @SuppressWarnings("unchecked")
        private void accept(Object object) {
            handle((T) object);
        }
        protected abstract void handle(T resp);
    }

    protected abstract void online();
    protected abstract void offline();
    protected void serverTime(long serverTime) {}

    protected int msgLength() {
        return 1024 * 1024 * 10;
    }

    protected String getCharset() {
        return "UTF-8";
    }

    protected String getPassword() {
        return null;
    }

}

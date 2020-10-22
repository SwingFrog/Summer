package com.swingfrog.summer.meter.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.swingfrog.summer.meter.MeterClient;
import com.swingfrog.summer.protocol.ProtocolConst;
import com.swingfrog.summer.protocol.protobuf.Protobuf;
import com.swingfrog.summer.protocol.protobuf.ReqProtobufMgr;
import com.swingfrog.summer.protocol.protobuf.RespProtobufMgr;
import com.swingfrog.summer.protocol.protobuf.proto.CommonProto;
import io.netty.channel.Channel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractMeterProtobufClient implements MeterClient {

    protected final int id;
    private Channel channel;

    private int waitRespId;
    private Callback<? extends Message> waitCallback;
    private final Queue<WaitSendReq> waitSendReqQueue = new ConcurrentLinkedQueue<>();

    private final ConcurrentMap<Integer, Push<? extends Message>> pushMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Queue<Push<? extends Message>>> oncePushMap = new ConcurrentHashMap<>();

    public AbstractMeterProtobufClient(int id) {
        this.id = id;
    }

    @Override
    public void sendHeartBeat() {
        CommonProto.HeartBeat_Req_0 req = CommonProto.HeartBeat_Req_0.getDefaultInstance();
        write(ProtocolConst.PROTOBUF_HEART_BEAT_REQ_ID, req);
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    void recv(Protobuf msg) {
        int messageId = msg.getId();
        byte[] bytes = msg.getBytes();
        Message messageTemplate = RespProtobufMgr.get().getMessageTemplate(messageId);
        if (messageTemplate == null) {
            System.err.println("messageId:" + messageId + " message template not exist");
            return;
        }
        try {
            Message message = messageTemplate.getParserForType().parseFrom(bytes);
            if (messageId == ProtocolConst.PROTOBUF_HEART_BEAT_REQ_ID) {
                CommonProto.HeartBeat_Resp_0 resp = (CommonProto.HeartBeat_Resp_0) message;
                heartBeat(resp.getTime());
            } else if (messageId == ProtocolConst.PROTOBUF_ERROR_CODE_RESP_ID) {
                CommonProto.ErrorCode_Resp_1 resp = (CommonProto.ErrorCode_Resp_1) message;
                if (waitRespId == resp.getReqId()) {
                    waitCallback.failure(resp.getCode(), resp.getMsg());
                    nextReq();
                }
            } else {
                if (waitRespId == messageId) {
                    waitCallback.accept(message);
                } else {
                    Push<? extends Message> push = pushMap.get(messageId);
                    if (push != null) {
                        push.accept(message);
                    }
                    Queue<Push<? extends Message>> pushQueue = oncePushMap.remove(messageId);
                    if (pushQueue != null) {
                        push = pushQueue.poll();
                        while (push != null) {
                            push.accept(message);
                            push = pushQueue.poll();
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    protected void req(Message message, Callback<? extends Message> callback) {
        Integer messageId = ReqProtobufMgr.get().getMessageId(message.getClass());
        if (messageId == null) {
            throw new RuntimeException(message.getClass().getSimpleName() + "message id not exist");
        }
        loadRespMessage(callback);
        if (waitSendReqQueue.isEmpty()) {
            sendReq(messageId, message, callback);
            return;
        }
        waitSendReqQueue.add(new WaitSendReq(messageId, message, callback));
    }

    private void nextReq() {
        WaitSendReq waitSendReq = waitSendReqQueue.poll();
        if (waitSendReq == null)
            return;
        sendReq(waitSendReq.messageId, waitSendReq.message, waitSendReq.callback);
    }

    private void sendReq(int messageId, Message message, Callback<? extends Message> callback) {
        waitRespId = messageId;
        waitCallback = callback;
        write(messageId, message);
    }

    protected void registerPush(Push<? extends Message> push) {
        int respId = loadRespMessage(push);
        pushMap.putIfAbsent(respId, push);
    }

    protected void oncePush(Push<? extends Message> push) {
        int respId = loadRespMessage(push);
        oncePushMap.computeIfAbsent(respId, (k) -> new ConcurrentLinkedQueue<>()).add(push);
    }

    private int loadRespMessage(Object object) {
        Class<? extends Message> messageClass = parseMessageClass(object);
        if (messageClass == null) {
            throw new RuntimeException("can not parse message class");
        }
        Integer respId = RespProtobufMgr.get().getMessageId(messageClass);
        if (respId == null) {
            throw new RuntimeException(messageClass.getSimpleName() + "message id not exist");
        }
        return respId;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Message> parseMessageClass(Object object) {
        Class<?> clazz = object.getClass();
        Type superClass = clazz.getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs != null && typeArgs.length > 0) {
                if (typeArgs[0] instanceof Class) {
                    return (Class<? extends Message>) typeArgs[0];
                }
            }
        }
        return null;
    }

    void write(int messageId, Message message) {
        if (!isActive())
            return;
        channel.writeAndFlush(Protobuf.of(messageId, message));
    }

    void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected static class WaitSendReq {
        private final int messageId;
        private final Message message;
        private final Callback<? extends Message> callback;
        public WaitSendReq(int messageId, Message message, Callback<? extends Message> callback) {
            this.messageId = messageId;
            this.message = message;
            this.callback = callback;
        }
    }

    protected static abstract class Callback<T extends Message> {
        @SuppressWarnings("unchecked")
        private void accept(Message message) {
            success((T) message);
        }
        protected abstract void success(T resp);
        protected abstract void failure(long code, String msg);
    }

    protected static abstract class Push<T extends Message> {
        @SuppressWarnings("unchecked")
        private void accept(Message message) {
            handle((T) message);
        }
        protected abstract void handle(T resp);
    }

    protected abstract void online();
    protected abstract void offline();
    protected void heartBeat(long serverTime) {}

    protected int msgLength() {
        return 1024 * 1024 * 10;
    }

}

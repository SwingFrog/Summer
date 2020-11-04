package com.swingfrog.summer.protocol.protobuf;

import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import com.swingfrog.summer.util.ProtobufUtil;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractProtobufMgr {

    private final ConcurrentMap<Integer, Message> messageTemplateMap;
    private final ConcurrentMap<Class<? extends Message>, Integer> messageIdMap;
    private volatile boolean openCheckProto = true;

    protected AbstractProtobufMgr() {
        messageTemplateMap = Maps.newConcurrentMap();
        messageIdMap = Maps.newConcurrentMap();
    }

    public void registerMessage(int messageId, Message messageTemplate) {
        if (messageTemplateMap.containsKey(messageId))
            return;
        if (openCheckProto)
            checkProto(messageTemplate);
        messageTemplateMap.putIfAbsent(messageId, messageTemplate);
        messageIdMap.putIfAbsent(messageTemplate.getClass(), messageId);
    }

    @Nullable
    public Message getMessageTemplate(int messageId) {
        return messageTemplateMap.get(messageId);
    }

    @Nullable
    public Integer getMessageId(Class<? extends Message> clazz) {
        Integer messageId = messageIdMap.get(clazz);
        if (messageId == null) {
            try {
                Message message = ProtobufUtil.getDefaultInstance(clazz);
                messageId = ProtobufUtil.getMessageId(message);
                registerMessage(messageId, message);
            } catch (Exception e) {
                return null;
            }
        }
        return messageId;
    }

    @Nullable
    public String getProtoName(int messageId) {
        Message message = getMessageTemplate(messageId);
        if (message == null)
            return null;
        return message.getClass().getSimpleName();
    }

    protected abstract void checkProto(Message messageTemplate);

    public void setOpenCheckProto(boolean openCheckProto) {
        this.openCheckProto = openCheckProto;
    }

}

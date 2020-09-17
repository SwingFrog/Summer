package com.swingfrog.summer.protocol.protobuf;

import com.google.protobuf.Message;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ProtobufMgr {

    private final Map<Integer, Message> messageTemplateMap;
    private final Map<Class<? extends Message>, Integer> messageIdMap;

    private static class SingleCase {
        public static final ProtobufMgr INSTANCE = new ProtobufMgr();
    }

    private ProtobufMgr() {
        messageTemplateMap = new HashMap<>();
        messageIdMap = new HashMap<>();
    }

    public static ProtobufMgr get() {
        return ProtobufMgr.SingleCase.INSTANCE;
    }

    public void registerMessage(int messageId, Message messageTemplate) {
        messageTemplateMap.putIfAbsent(messageId, messageTemplate);
        messageIdMap.putIfAbsent(messageTemplate.getClass(), messageId);
    }

    @Nullable
    public Message getMessageTemplate(int messageId) {
        return messageTemplateMap.get(messageId);
    }

    @Nullable
    public Integer getMessageId(Class<? extends Message> clazz) {
        return messageIdMap.get(clazz);
    }

    @Nullable
    public String getProtoName(int messageId) {
        Message message = messageTemplateMap.get(messageId);
        if (message == null)
            return null;
        return message.getClass().getSimpleName();
    }

}

package com.swingfrog.summer.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProtobufUtil {

    public static int getMessageId(Message message) {
        return getMessageId(message.getClass());
    }

    public static int getMessageId(Class<? extends Message> clazz) {
        String name = clazz.getSimpleName();
        int index = name.lastIndexOf("_");
        if (index <= -1) {
            return -1;
        }
        try {
            String target = name.substring(index + 1);
            return Integer.parseInt(target);
        } catch (Exception e) {
            return -1;
        }
    }

    public static Message getDefaultInstance(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getDeclaredMethod("getDefaultInstance");
        return (Message) method.invoke(null);
    }

    public static Message parseMessage(Message message, byte[] bytes) throws InvalidProtocolBufferException {
        return message.getParserForType().parseFrom(bytes);
    }

    public static int getMessageSize(Message message) {
        return message.getSerializedSize();
    }

}

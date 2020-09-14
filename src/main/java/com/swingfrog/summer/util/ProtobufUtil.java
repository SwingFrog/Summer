package com.swingfrog.summer.util;

import com.google.protobuf.Message;

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

}

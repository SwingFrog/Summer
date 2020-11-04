package com.swingfrog.summer.protocol.protobuf;

import com.google.protobuf.Message;

public class RespProtobufMgr extends AbstractProtobufMgr {

    private static final String PREFIX_RESP = "resp";
    private static final String PREFIX_PUSH = "push";

    private static class SingleCase {
        public static final RespProtobufMgr INSTANCE = new RespProtobufMgr();
    }

    public static RespProtobufMgr get() {
        return RespProtobufMgr.SingleCase.INSTANCE;
    }

    @Override
    protected void checkProto(Message messageTemplate) {
        String protoName = messageTemplate.getClass().getSimpleName().toLowerCase();
        if (!protoName.contains(PREFIX_RESP) && !protoName.contains(PREFIX_PUSH))
            throw new IllegalArgumentException("not resp proto -> " + messageTemplate.getClass().getName());
    }

}

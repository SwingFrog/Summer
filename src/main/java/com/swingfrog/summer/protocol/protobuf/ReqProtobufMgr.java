package com.swingfrog.summer.protocol.protobuf;

import com.google.protobuf.Message;

public class ReqProtobufMgr extends AbstractProtobufMgr {

    private static final String PREFIX_REQ = "req";

    private static class SingleCase {
        public static final ReqProtobufMgr INSTANCE = new ReqProtobufMgr();
    }

    public static ReqProtobufMgr get() {
        return ReqProtobufMgr.SingleCase.INSTANCE;
    }

    @Override
    protected void checkProto(Message messageTemplate) {
        String protoName = messageTemplate.getClass().getSimpleName().toLowerCase();
        if (!protoName.contains(PREFIX_REQ))
            throw new IllegalArgumentException("not req proto -> " + messageTemplate.getClass().getName());
    }

}

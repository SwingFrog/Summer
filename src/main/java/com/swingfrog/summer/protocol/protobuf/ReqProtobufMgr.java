package com.swingfrog.summer.protocol.protobuf;

public class ReqProtobufMgr extends AbstractProtobufMgr {

    private static class SingleCase {
        public static final ReqProtobufMgr INSTANCE = new ReqProtobufMgr();
    }

    public static ReqProtobufMgr get() {
        return ReqProtobufMgr.SingleCase.INSTANCE;
    }

}

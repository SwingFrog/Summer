package com.swingfrog.summer.protocol.protobuf;

public class RespProtobufMgr extends AbstractProtobufMgr {

    private static class SingleCase {
        public static final RespProtobufMgr INSTANCE = new RespProtobufMgr();
    }

    public static RespProtobufMgr get() {
        return RespProtobufMgr.SingleCase.INSTANCE;
    }

}

package com.swingfrog.summer.protocol.protobuf;

import com.google.protobuf.Message;
import com.swingfrog.summer.protocol.protobuf.proto.CommonProto;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.CodeMsg;

public class ErrorCodeProtobufBuilder {

    public static Message build(int reqId, long code, String msg) {
        return CommonProto.ErrorCode_Resp_1
                .newBuilder()
                .setReqId(reqId)
                .setCode(code)
                .setMsg(msg)
                .build();
    }

    public static Message build(int reqId, CodeMsg codeMsg) {
        return build(reqId, codeMsg.getCode(), codeMsg.getMsg());
    }

    public static Message build(int reqId, CodeException codeException) {
        return build(reqId, codeException.getCode(), codeException.getMsg());
    }

}

package com.swingfrog.summer.test.ecsgameserver.infrastructure;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.server.exception.CodeMsg;

import java.util.stream.Stream;

public enum ErrorCode {

    ;

    private final CodeMsg codeMsg;

    ErrorCode(long code, String msg) {
        codeMsg = Summer.createCodeMsg(code, msg);
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }

    public static ErrorCode getErrorCode(long code) {
        return Stream.of(values()).filter(errorCode -> errorCode.getCodeMsg().getCode() == code).findAny().orElse(null);
    }

}

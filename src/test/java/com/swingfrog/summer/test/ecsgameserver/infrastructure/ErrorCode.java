package com.swingfrog.summer.test.ecsgameserver.infrastructure;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.server.exception.CodeMsg;

import java.util.stream.Stream;

public enum ErrorCode {
    PLAYER_NOT_EXIST(10000, "player not exist"),

    RESOURCE_NOT_ENOUGH(11000, "resource not enough"),

    TEAM_NOT_EXIST(12000, "team not exist"),
    TEAM_JOIN_ALREADY(12001, "team join already"),
    ;

    private final CodeMsg codeMsg;

    ErrorCode(int code, String msg) {
        codeMsg = Summer.createCodeMsg(code, msg);
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }

    public static ErrorCode getErrorCode(int code) {
        return Stream.of(values()).filter(errorCode -> errorCode.getCodeMsg().getCode() == code).findAny().orElse(null);
    }

}

package com.swingfrog.summer.protocol.tiny.msg;

public interface TinyConst {

    int ID_PING = 0;

    byte ORDER_PONG = 0;
    byte ORDER_ERROR = 1;

    byte ORDER_RESP_JSON = 11;
    byte ORDER_RESP_ZIP = 12;

    byte ORDER_PUSH_JSON = 21;
    byte ORDER_PUSH_ZIP = 22;

    String ZIP = "data";

}

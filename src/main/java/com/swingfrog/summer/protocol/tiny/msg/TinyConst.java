package com.swingfrog.summer.protocol.tiny.msg;

public class TinyConst {

    public static final short ID_PING = 0;

    public static final byte ORDER_PONG = 0;
    public static final byte ORDER_ERROR = 1;

    public static final byte ORDER_RESP_JSON = 11;
    public static final byte ORDER_RESP_ZIP = 12;

    public static final byte ORDER_PUSH_JSON = 21;
    public static final byte ORDER_PUSH_ZIP = 22;

    public static final String ZIP_ENTRY_NAME = "data";

    public static int ZIP_USE_THRESHOLD = 200;

}

package com.swingfrog.summer.protocol.tiny.msg;

public class TinyReq {

    private final short id;
    private final String msg;

    public TinyReq(short id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public short getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "TinyReq{" +
                "id=" + id +
                ", msg='" + msg + '\'' +
                '}';
    }

}

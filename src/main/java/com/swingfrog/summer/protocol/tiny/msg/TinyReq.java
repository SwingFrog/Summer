package com.swingfrog.summer.protocol.tiny.msg;

public class TinyReq {

    private final int id;
    private final String msg;

    public TinyReq(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public int getId() {
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

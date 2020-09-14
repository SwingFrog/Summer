package com.swingfrog.summer.protocol.protobuf;

public class Protobuf {

    private final int id;
    private final byte[] bytes;

    public Protobuf(int id, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
    }

    public int getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

}

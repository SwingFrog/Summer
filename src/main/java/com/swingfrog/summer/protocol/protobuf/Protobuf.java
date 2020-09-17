package com.swingfrog.summer.protocol.protobuf;

import com.google.protobuf.Message;

public class Protobuf {

    private final int id;
    private final byte[] bytes;

    private Protobuf(int id, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
    }

    public static Protobuf of(int id, byte[] bytes) {
        return new Protobuf(id, bytes);
    }

    public static Protobuf of(int id, Message message) {
        return new Protobuf(id, message.toByteArray());
    }

    public int getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

}

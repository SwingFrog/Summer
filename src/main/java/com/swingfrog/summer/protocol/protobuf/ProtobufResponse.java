package com.swingfrog.summer.protocol.protobuf;

import com.google.protobuf.Message;

import java.util.Objects;

public class ProtobufResponse {

    private int id;
    private Message message;

    public static ProtobufResponse of(int id, Message message) {
        ProtobufResponse protobufResponse = new ProtobufResponse();
        protobufResponse.setId(id);
        protobufResponse.setMessage(message);
        return protobufResponse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtobufResponse that = (ProtobufResponse) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Protobuf toProtobuf() {
        return new Protobuf(id, message.toByteArray());
    }

}

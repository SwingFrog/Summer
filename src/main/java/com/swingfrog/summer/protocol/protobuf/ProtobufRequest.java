package com.swingfrog.summer.protocol.protobuf;

import java.util.Objects;

/**
 * for easy access on session handler
 */
public class ProtobufRequest {

    private int id;

    public static ProtobufRequest of(int id) {
        ProtobufRequest protobufRequest = new ProtobufRequest();
        protobufRequest.setId(id);
        return protobufRequest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtobufRequest that = (ProtobufRequest) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

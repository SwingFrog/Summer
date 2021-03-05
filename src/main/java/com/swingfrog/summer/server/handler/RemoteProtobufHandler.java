package com.swingfrog.summer.server.handler;

import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.server.SessionContext;

public interface RemoteProtobufHandler {

    void handleReady(SessionContext ctx, ProtobufRequest request);

}

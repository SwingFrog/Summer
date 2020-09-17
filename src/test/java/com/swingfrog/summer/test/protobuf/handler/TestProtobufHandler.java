package com.swingfrog.summer.test.protobuf.handler;

import com.swingfrog.summer.annotation.ServerHandler;
import com.swingfrog.summer.protocol.protobuf.ProtobufRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerHandler
public class TestProtobufHandler implements SessionHandler {

    private static final Logger log = LoggerFactory.getLogger(TestProtobufHandler.class);

    @Override
    public boolean receive(SessionContext ctx, ProtobufRequest request) {
        int messageId = request.getId();
        log.info("TestProtobufHandler.receive messageId[{}]", messageId);
        return true;
    }

}

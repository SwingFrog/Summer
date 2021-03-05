package com.swingfrog.summer.server.handler;

import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;

public interface RemoteHandler {

    void handleReady(SessionContext ctx, SessionRequest request);

}

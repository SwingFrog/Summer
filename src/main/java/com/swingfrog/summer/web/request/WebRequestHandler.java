package com.swingfrog.summer.web.request;

import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;

public interface WebRequestHandler {

    default WebRequest getWebRequest(SessionContext sctx, WebRequest request) {
        return request;
    }

}

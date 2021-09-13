package com.swingfrog.summer.test.web.model;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.PlainView;

public class CommonResp extends PlainView {

    @Override
    protected String getText(ServerContext serverContext, SessionContext sctx, WebRequest request) {
        return request.getPath();
    }

}

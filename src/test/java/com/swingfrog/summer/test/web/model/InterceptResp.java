package com.swingfrog.summer.test.web.model;

import com.swingfrog.summer.server.ServerContext;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.WebView;
import com.swingfrog.summer.web.view.render.WebViewRender;

public class InterceptResp implements WebView {

    private final String msg;

    public InterceptResp(String msg) {
        this.msg = msg;
    }

    @Override
    public WebViewRender onRender(ServerContext serverContext, SessionContext sctx, WebRequest request) throws Exception {
        return null;
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getContentType() {
        return "";
    }

    public String getMsg() {
        return msg;
    }
}

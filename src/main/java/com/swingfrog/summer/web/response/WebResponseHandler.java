package com.swingfrog.summer.web.response;

import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.view.WebView;
import io.netty.handler.codec.http.HttpResponse;

public interface WebResponseHandler {

    default WebView getWebView(SessionContext sctx, WebRequest request, WebView webView) {
        return webView;
    }

    default HttpResponse getHttpResponse(SessionContext sctx, WebRequest request, WebView webView, HttpResponse response) {
        return response;
    }

}

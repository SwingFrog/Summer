package com.swingfrog.summer.test.web;

import com.alibaba.fastjson.JSONObject;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.test.web.model.InterceptResp;
import com.swingfrog.summer.web.WebMgr;
import com.swingfrog.summer.web.WebRequest;
import com.swingfrog.summer.web.response.WebResponseHandler;
import com.swingfrog.summer.web.token.WebTokenHandler;
import com.swingfrog.summer.web.view.HtmlView;
import com.swingfrog.summer.web.view.InteriorViewFactory;
import com.swingfrog.summer.web.view.JSONView;
import com.swingfrog.summer.web.view.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TestWebBootstrap implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(TestWebBootstrap.class);

    @Override
    public void init() {
        log.info("init");
    }

    @Override
    public void start() {
        log.info("start");
    }

    @Override
    public void stop() {
        log.info("stop");
    }

    public static void main(String[] args) {
        Summer.addModuleNet();
        String resources = TestWebBootstrap.class.getClassLoader().getResource("web").getPath();
        Summer.hot(SummerConfig.newBuilder()
                .app(new TestWebBootstrap())
                .dbProperties(resources + "/db.properties")
                .redisProperties(resources + "/redis.properties")
                .serverProperties(resources + "/server.properties")
                .taskProperties(resources + "/task.properties")
                .build());
        WebMgr.get().setTemplatePath(resources + "/Template");
        WebMgr.get().setWebContentPath(resources + "/WebContent");
        WebMgr.get().setInteriorViewFactory(new InteriorViewFactory() {
            @Override
            public WebView createErrorView(int status, String msg) {
                JSONObject json = new JSONObject();
                json.put("status", status);
                json.put("msg", msg);
                return new JSONView(json);
            }
            @Override
            public WebView createErrorView(int status, long code, String msg) {
                JSONObject json = new JSONObject();
                json.put("status", status);
                json.put("code", code);
                json.put("msg", msg);
                return new JSONView(json);
            }
        });
        WebMgr.get().setWebTokenHandler(new WebTokenHandler() {
            @Override
            public String createToken() {
                return "test_token=" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
            }

            @Override
            public String parseToken(String cookie) {
                if (cookie == null)
                    return null;
                String token = "test_token=";
                int index = cookie.indexOf(token);
                if (index == -1)
                    return null;
                if (index + token.length() + 32 > cookie.length()) {
                    return null;
                }
                return cookie.substring(index + token.length(), index + token.length() + 32);
            }
        });
        WebMgr.get().setIndex("TestRemote_hello");
        WebMgr.get().setWebResponseHandler(new WebResponseHandler() {
            @Override
            public WebView getWebView(SessionContext sctx, WebRequest request, WebView webView) {
                if (webView instanceof InterceptResp) {
                    InterceptResp interceptResp = (InterceptResp) webView;
                    return HtmlView.of("msg: " + interceptResp.getMsg());
                }
                return webView;
            }
        });

        // http://127.0.0.1:8080/TestRemote_hello
        // http://127.0.0.1:8080/TestRemote_add?a=1&b=2
        // http://127.0.0.1:8080/TestRemote_say?msg=hi
        // http://127.0.0.1:8080/TestRemote_template?msg=hi
        // http://127.0.0.1:8080/upload.html
        // http://127.0.0.1:8080/TestRemote_asyncHello
        // http://127.0.0.1:8080/custom_request_mapping
        // http://127.0.0.1:8080/TestRemote_paramPacking?id=123&name=abc
        // http://127.0.0.1:8080/TestRemote_getToken
        // http://127.0.0.1:8080/TestRemote_clearToken
        // http://127.0.0.1:8080/TestRemote_getCommonResp
        // http://127.0.0.1:8080/TestRemote_intercept
    }

}

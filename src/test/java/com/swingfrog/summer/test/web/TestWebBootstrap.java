package com.swingfrog.summer.test.web;

import com.alibaba.fastjson.JSONObject;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;
import com.swingfrog.summer.app.SummerConfig;
import com.swingfrog.summer.web.WebMgr;
import com.swingfrog.summer.web.view.InteriorViewFactory;
import com.swingfrog.summer.web.view.JSONView;
import com.swingfrog.summer.web.view.WebView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestWebBootstrap implements SummerApp {

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

        // http://127.0.0.1:8080/TestRemote_hello
        // http://127.0.0.1:8080/TestRemote_add?a=1&b=2
        // http://127.0.0.1:8080/TestRemote_say?msg=hi
        // http://127.0.0.1:8080/TestRemote_template?msg=hi
        // http://127.0.0.1:8080/upload.html
        // http://127.0.0.1:8080/TestRemote_asyncHello
    }

}

package com.swingfrog.summer.test.web.remote;

import com.swingfrog.summer.annotation.*;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.async.AsyncResponse;
import com.swingfrog.summer.server.handler.RemoteHandler;
import com.swingfrog.summer.test.web.model.CommonResp;
import com.swingfrog.summer.test.web.model.TestModel;
import com.swingfrog.summer.test.web.service.TestService;
import com.swingfrog.summer.web.WebFileUpload;
import com.swingfrog.summer.web.view.ModelView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Remote
public class TestRemote implements RemoteHandler {

    @Autowired
    private TestService testService;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public String hello() {
        return "hello world!";
    }

    public int add(int a, @Optional("2") int b) {
        return testService.add(a, b);
    }

    public String say(SessionContext sctx, String msg) {
        return String.format("sessionId[%s]: %s %s", sctx.getSessionId(), msg,
                sctx.getSessionId() == null ? "tips: The first time you access this interface, the sessionId is null because the browser has no cookies" : "");
    }

    public ModelView template(String msg) {
        ModelView modelView = new ModelView("msg.html");
        modelView.put("msg", msg);
        return modelView;
    }

    public ModelView uploadFile(WebFileUpload upload) {
        ModelView modelView = new ModelView("msg.html");
        if (upload.isEmpty()) {
            modelView.put("msg", "未选择文件");
        } else {
            modelView.put("msg", upload.getFileName());
        }
        return modelView;
    }

    public AsyncResponse asyncHello(SessionContext sctx, SessionRequest request) {
        executor.execute(() -> Summer.asyncResponse(sctx, request, () -> "hello async world!"));
        return AsyncResponse.of();
    }

    @RequestMapping("custom_request_mapping")
    public String customRequestMapping() {
        return "custom_request_mapping";
    }

    public TestModel paramPacking(@ParamPacking TestModel testModel, String name) {
        System.out.println(name);
        return testModel;
    }

    public String getToken(SessionContext sctx) {
        return sctx.getToken();
    }

    public void clearToken(SessionContext sctx) {
        sctx.clearToken();
    }

    public CommonResp getCommonResp() {
        return new CommonResp();
    }

    @Override
    public void handleReady(SessionContext ctx, SessionRequest request) {
        System.out.println("TestRemote.handleReady " + request);
    }
}

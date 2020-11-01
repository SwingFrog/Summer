package com.swingfrog.summer.test.ecsgameserver.infrastructure;

import com.swingfrog.summer.annotation.Component;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.lifecycle.Lifecycle;
import com.swingfrog.summer.lifecycle.LifecycleInfo;
import com.swingfrog.summer.promise.Promise;
import com.swingfrog.summer.promise.PromisePool;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.exception.CodeException;
import com.swingfrog.summer.server.exception.SessionException;
import com.swingfrog.summer.test.ecsgameserver.module.player.Player;

@Component
public class PromiseManager extends PromisePool implements Lifecycle {

    @Override
    public LifecycleInfo getInfo() {
        return LifecycleInfo.build("PromiseManager");
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        shutdown();
    }

    public Promise createPlayerPromise(Player player, SessionContext ctx, SessionRequest req) {
        return super.createPromise()
                .setExecutor(player.getExecutor())
                .setCatch(throwable -> {
                    if (throwable instanceof CodeException) {
                        CodeException codeException = (CodeException) throwable;
                        Summer.asyncResponse(ctx, req, codeException.getCode(), codeException.getMsg());
                    } else {
                        Summer.asyncResponse(ctx, req, SessionException.INVOKE_ERROR.getCode(), SessionException.INVOKE_ERROR.getMsg());
                    }
                });
    }

}

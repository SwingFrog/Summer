package com.swingfrog.summer.test.ecsgameserver.module.login;

import com.swingfrog.summer.annotation.ServerHandler;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.SessionHandler;
import com.swingfrog.summer.test.ecsgameserver.infrastructure.SessionHandlerPriority;

@ServerHandler
public class LoginHandler implements SessionHandler {

    @Override
    public int priority() {
        return SessionHandlerPriority.LOGIN;
    }

    @Override
    public boolean receive(SessionContext ctx, SessionRequest request) {
        if (ctx.getToken() == null) {
            return LoginRemote.class.getSimpleName().equals(request.getRemote());
        }
        return true;
    }

    @Override
    public void heartTimeOut(SessionContext sessionContext) {
        Summer.closeSession(sessionContext);
    }

    @Override
    public void lengthTooLongMsg(SessionContext sessionContext) {
        Summer.closeSession(sessionContext);
    }

    @Override
    public void unableParseMsg(SessionContext sessionContext) {
        Summer.closeSession(sessionContext);
    }

    @Override
    public void repetitionMsg(SessionContext sessionContext) {
        Summer.closeSession(sessionContext);
    }

}

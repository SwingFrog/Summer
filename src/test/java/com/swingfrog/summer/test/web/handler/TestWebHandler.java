package com.swingfrog.summer.test.web.handler;

import com.swingfrog.summer.annotation.ServerHandler;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.SessionHandler;

@ServerHandler
public class TestWebHandler implements SessionHandler {

    @Override
    public boolean accpet(SessionContext ctx) {
        return true;
    }

    @Override
    public void added(SessionContext ctx) {

    }

    @Override
    public void removed(SessionContext ctx) {

    }

    @Override
    public boolean receive(SessionContext ctx, SessionRequest request) {
        return true;
    }

    @Override
    public void heartTimeOut(SessionContext ctx) {

    }

    @Override
    public void sendTooFastMsg(SessionContext ctx) {

    }

    @Override
    public void lengthTooLongMsg(SessionContext ctx) {

    }

    @Override
    public void unableParseMsg(SessionContext ctx) {

    }

    @Override
    public void repetitionMsg(SessionContext ctx) {

    }

    @Override
    public void sending(SessionContext ctx) {

    }

}

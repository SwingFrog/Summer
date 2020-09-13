package com.swingfrog.summer.test.server.master.handler;

import com.swingfrog.summer.annotation.ServerHandler;
import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.protocol.SessionRequest;
import com.swingfrog.summer.server.SessionContext;
import com.swingfrog.summer.server.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerHandler
public class MasterHandler implements SessionHandler {

    private static final Logger log = LoggerFactory.getLogger(MasterHandler.class);

    @Override
    public boolean accept(SessionContext ctx) {
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
        log.error("{} time out", ctx);
        Summer.closeSession(ctx);
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

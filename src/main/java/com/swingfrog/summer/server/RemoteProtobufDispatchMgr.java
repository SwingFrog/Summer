package com.swingfrog.summer.server;

import com.swingfrog.summer.annotation.Remote;
import com.swingfrog.summer.ioc.ContainerMgr;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class RemoteProtobufDispatchMgr {

    private static final Logger log = LoggerFactory.getLogger(RemoteProtobufDispatchMgr.class);


    private static class SingleCase {
        public static final RemoteProtobufDispatchMgr INSTANCE = new RemoteProtobufDispatchMgr();
    }

    private RemoteProtobufDispatchMgr() {

    }

    public static RemoteProtobufDispatchMgr get() {
        return RemoteProtobufDispatchMgr.SingleCase.INSTANCE;
    }

    public void init() throws NotFoundException {
        Iterator<Class<?>> ite = ContainerMgr.get().iteratorRemoteList();
        while (ite.hasNext()) {
            Class<?> clazz = ite.next();
            Remote remote = clazz.getAnnotation(Remote.class);
            if (!remote.protobuf()) {
                continue;
            }
            log.info("server register remote protobuf {}", clazz.getSimpleName());

        }
    }

}

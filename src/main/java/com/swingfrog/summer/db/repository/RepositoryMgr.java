package com.swingfrog.summer.db.repository;

import com.swingfrog.summer.ioc.ContainerMgr;

public class RepositoryMgr {

    private static class SingleCase {
        public static final RepositoryMgr INSTANCE = new RepositoryMgr();
    }

    private RepositoryMgr() {

    }

    public static RepositoryMgr get() {
        return RepositoryMgr.SingleCase.INSTANCE;
    }

    public void init() {
        ContainerMgr.get().listDeclaredComponent(RepositoryDao.class).forEach(RepositoryDao::init);
    }

}

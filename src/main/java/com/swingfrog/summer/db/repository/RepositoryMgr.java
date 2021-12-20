package com.swingfrog.summer.db.repository;

import com.google.common.collect.Maps;
import com.swingfrog.summer.ioc.ContainerMgr;

import javax.annotation.Nullable;
import java.util.Map;

public class RepositoryMgr {

    private static final Map<Class<?>, Repository<?, ?>> map = Maps.newHashMap();

    private static class SingleCase {
        public static final RepositoryMgr INSTANCE = new RepositoryMgr();
    }

    private RepositoryMgr() {

    }

    public static RepositoryMgr get() {
        return RepositoryMgr.SingleCase.INSTANCE;
    }

    public void init() {
        for (RepositoryDao<?, ?> repositoryDao : ContainerMgr.get().listDeclaredComponent(RepositoryDao.class)) {
            Class<?> entityClass = repositoryDao.getEntityClass();
            map.put(entityClass, repositoryDao);
            repositoryDao.init();
        }
    }

    @Nullable
    public <T> Repository<T, ?> findRepository(Class<T> entityClass) {
        return (Repository<T, ?>) map.get(entityClass);
    }

}

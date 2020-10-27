package com.swingfrog.summer.test.ecsgameserver.infrastructure;

import com.swingfrog.summer.db.repository.AsyncCacheRepositoryDao;

public abstract class BaseRepository<V, K> extends AsyncCacheRepositoryDao<V, K> {

    @Override
    protected long delayTime() {
        return 300000;
    }

    @Override
    protected long expireTime() {
        return 1000000;
    }

}

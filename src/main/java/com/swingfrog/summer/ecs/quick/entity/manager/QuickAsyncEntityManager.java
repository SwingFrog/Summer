package com.swingfrog.summer.ecs.quick.entity.manager;

import com.swingfrog.summer.ecs.entity.AsyncEntity;
import com.swingfrog.summer.ecs.entity.mananger.AbstractAsyncEntityManager;
import com.swingfrog.summer.ecs.quick.entity.QuickAsyncEntity;

public abstract class QuickAsyncEntityManager<E extends AsyncEntity<Long>> extends AbstractAsyncEntityManager<Long, E> {
}

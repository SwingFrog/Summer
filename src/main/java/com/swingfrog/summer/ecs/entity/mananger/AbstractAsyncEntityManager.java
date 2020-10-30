package com.swingfrog.summer.ecs.entity.mananger;

import com.swingfrog.summer.ecs.entity.AsyncEntity;
import com.swingfrog.summer.promise.Promise;
import com.swingfrog.summer.promise.PromiseValueConsumer;

import java.util.function.Consumer;

public abstract class AbstractAsyncEntityManager<K, E extends AsyncEntity<K>> extends AbstractEntityManager<K, E> {

    public Promise.ConsumerTask promiseEntity(K entityId, PromiseValueConsumer<E> consumer) {
        E entity = getEntity(entityId);
        return Promise.newTask(context -> consumer.accept(context, entity), entity.getExecutor());
    }

    public Promise.RunnableTask promiseEntity(K entityId, Consumer<E> consumer) {
        E entity = getEntity(entityId);
        return Promise.newTask(() -> consumer.accept(entity), entity.getExecutor());
    }

    public void acceptEntity(K entityId, Consumer<E> consumer) {
        E entity = getEntity(entityId);
        entity.getExecutor().execute(() -> consumer.accept(entity));
    }

}

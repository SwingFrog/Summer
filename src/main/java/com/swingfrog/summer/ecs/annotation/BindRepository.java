package com.swingfrog.summer.ecs.annotation;

import com.swingfrog.summer.db.repository.Repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BindRepository {

    Class<? extends Repository<?, ?>> value();

}

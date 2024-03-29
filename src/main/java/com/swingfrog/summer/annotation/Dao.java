package com.swingfrog.summer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.swingfrog.summer.annotation.base.AutowiredManager;
import com.swingfrog.summer.annotation.base.ComponentManager;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ComponentManager
@AutowiredManager
public @interface Dao {

}

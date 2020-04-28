package com.swingfrog.summer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.swingfrog.summer.annotation.base.AutowiredManager;
import com.swingfrog.summer.annotation.base.Component;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
@AutowiredManager
public @interface Bean {

}

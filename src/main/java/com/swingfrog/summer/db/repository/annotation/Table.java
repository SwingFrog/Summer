package com.swingfrog.summer.db.repository.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    String name() default "";
    String charset() default "";
    String collate() default "";
    String comment() default "";

}

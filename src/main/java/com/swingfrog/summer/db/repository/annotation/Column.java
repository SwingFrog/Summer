package com.swingfrog.summer.db.repository.annotation;

import com.swingfrog.summer.db.repository.ColumnType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    ColumnType type() default ColumnType.DEFAULT;
    boolean readOnly() default false;
    boolean nonNull() default false;
    boolean unsigned() default false;
    int length() default 255;
    String comment() default "";

}

package org.metavm.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntityStruct {

    String value() default "";

    boolean compiled() default false;

    boolean anonymous() default false;

    boolean ephemeral() default false;

}

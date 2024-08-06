package org.metavm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ChildEntity {

    String value() default "";

    boolean lazy() default false;

    boolean removed() default false;

    int tag() default -1;

}

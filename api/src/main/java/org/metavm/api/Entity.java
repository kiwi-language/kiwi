package org.metavm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    String value() default "";

    boolean compiled() default false;

    boolean ephemeral() default false;

    boolean searchable() default false;

    int tag() default -1;

    int since() default 0;

}

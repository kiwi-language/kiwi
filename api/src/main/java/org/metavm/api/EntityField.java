package org.metavm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface EntityField {

    String value() default "";

    boolean asTitle() default false;

    boolean asKey() default false;

    boolean eager() default false;

    String code() default "";

    boolean removed() default false;

    int tag() default -1;

    int since() default 0;

}

package org.metavm.context.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
public @interface RequestParam {

    String value();

    String defaultValue() default "";

    boolean required() default true;

}

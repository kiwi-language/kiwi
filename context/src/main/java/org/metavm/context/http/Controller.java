package org.metavm.context.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Controller {

    String module() default "";

}

package org.manul.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Init {

    int order() default Integer.MAX_VALUE;

}

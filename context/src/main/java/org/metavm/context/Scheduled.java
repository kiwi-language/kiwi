package org.metavm.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Scheduled {

    long fixedDelay();

}

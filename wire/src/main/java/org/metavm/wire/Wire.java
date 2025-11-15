package org.metavm.wire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Wire {

    int value() default -1;

    Class<? extends WireAdapter> adapter() default WireAdapter.class;

    SubType[] subTypes() default {};

}

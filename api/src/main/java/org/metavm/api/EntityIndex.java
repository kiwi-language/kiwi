package org.metavm.api;

public @interface EntityIndex {

    String value() default "";

    boolean unique() default false;

}

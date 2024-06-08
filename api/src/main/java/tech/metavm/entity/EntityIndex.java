package tech.metavm.entity;

public @interface EntityIndex {

    String value() default "";

    boolean unique() default false;

}

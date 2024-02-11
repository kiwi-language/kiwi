package tech.metavm.entity;

public @interface EntityIndex {

    String value();

    boolean unique() default false;

}

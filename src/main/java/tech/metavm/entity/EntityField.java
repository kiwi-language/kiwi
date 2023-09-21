package tech.metavm.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface EntityField {

    String value();

    boolean unique() default false;

    boolean asTitle() default false;

    boolean eager() default false;

    String code() default "";

}

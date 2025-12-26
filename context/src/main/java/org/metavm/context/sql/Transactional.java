package org.metavm.context.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Transactional {

    boolean readonly() default false;

    TransactionPropagation propagation() default TransactionPropagation.REQUIRED;

    TransactionIsolation isolation() default TransactionIsolation.READ_COMMITTED;

}

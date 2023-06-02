package tech.metavm.spoon;

import java.util.function.Supplier;

public class ExceptionNewRefLab {

    public Supplier<? extends Exception> getExceptionSupplier() {
        return NullPointerException::new;
    }

}

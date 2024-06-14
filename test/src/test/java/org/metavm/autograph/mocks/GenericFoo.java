package org.metavm.autograph.mocks;

public class GenericFoo<E> extends GenericBase<E> {

    public static GenericFoo<String> INSTANCE;

    public static void setInstanceValue() {
        INSTANCE.setValue("Hello");
    }

    public void foo(E value) {
        setValue(value);
    }


}

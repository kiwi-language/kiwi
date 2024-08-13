package org.metavm.entity;

public interface DefContextHolder {

    DefContext get();

    void set(DefContext defContext);

    boolean isPresent();

    default void clearLocal() {
        throw new UnsupportedOperationException();
    }

}

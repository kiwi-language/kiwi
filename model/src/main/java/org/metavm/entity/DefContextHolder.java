package org.metavm.entity;

public interface DefContextHolder {

    SystemDefContext get();

    void set(SystemDefContext defContext);

    boolean isPresent();
}

package org.metavm.entity;

public interface Element {

    <R> R accept(ElementVisitor<R> visitor);

    void acceptChildren(ElementVisitor<?> visitor);

}

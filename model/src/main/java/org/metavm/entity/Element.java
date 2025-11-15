package org.metavm.entity;

import org.metavm.api.Entity;

@Entity
public interface Element {

    <R> R accept(ElementVisitor<R> visitor);

    void acceptChildren(ElementVisitor<?> visitor);

}

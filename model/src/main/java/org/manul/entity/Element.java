package org.manul.entity;

import org.manul.api.Entity;

@Entity
public interface Element {

    <R> R accept(ElementVisitor<R> visitor);

    void acceptChildren(ElementVisitor<?> visitor);

}

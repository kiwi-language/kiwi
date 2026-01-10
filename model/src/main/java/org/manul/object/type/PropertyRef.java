package org.manul.object.type;

import org.manul.api.Entity;
import org.manul.entity.Reference;
import org.manul.entity.Writable;

import java.util.function.Consumer;

@Entity
public interface PropertyRef extends Reference, Writable {

    Type getPropertyType();

    Property getProperty();

    String getName();

    ClassType getDeclaringType();

    void forEachReference(Consumer<org.manul.object.instance.core.Reference> action);
}

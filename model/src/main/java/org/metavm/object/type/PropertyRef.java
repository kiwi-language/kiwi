package org.metavm.object.type;

import org.metavm.entity.Writable;
import org.metavm.entity.Reference;

import java.util.function.Consumer;

public interface PropertyRef extends Reference, Writable {

    Type getPropertyType();

    Property getProperty();

    String getName();

    ClassType getDeclaringType();

    void forEachReference(Consumer<org.metavm.object.instance.core.Reference> action);
}

package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.Reference;
import org.metavm.entity.Writable;

import java.util.function.Consumer;

@Entity
public interface PropertyRef extends Reference, Writable {

    Type getPropertyType();

    Property getProperty();

    String getName();

    ClassType getDeclaringType();

    void forEachReference(Consumer<org.metavm.object.instance.core.Reference> action);
}

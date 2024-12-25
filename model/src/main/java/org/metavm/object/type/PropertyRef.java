package org.metavm.object.type;

import org.metavm.entity.Writable;
import org.metavm.entity.Reference;

public interface PropertyRef extends Reference, Writable {

    Type getPropertyType();

    Property getProperty();

    String getName();

    ClassType getDeclaringType();
}

package org.metavm.object.type;

import org.metavm.entity.Reference;

public interface PropertyRef extends Reference {

    Type getType();

    Property getProperty();

    String getName();

    ClassType getDeclaringType();
}

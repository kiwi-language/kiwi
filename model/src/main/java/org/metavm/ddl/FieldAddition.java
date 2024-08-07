package org.metavm.ddl;

import org.metavm.api.EntityType;
import org.metavm.api.ValueObject;
import org.metavm.flow.Method;
import org.metavm.object.type.Klass;

@EntityType
public record FieldAddition(
        Klass klass,
        int fieldTag,
        Method initializer
) implements ValueObject {
}

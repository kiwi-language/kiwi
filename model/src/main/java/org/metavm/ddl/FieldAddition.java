package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.flow.Method;
import org.metavm.object.type.Field;

@Entity
public record FieldAddition(
        Field field,
        Method initializer
) implements ValueObject {
}

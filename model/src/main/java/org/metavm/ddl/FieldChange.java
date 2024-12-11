package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;

@Entity
public record FieldChange(
        String klassId,
        String fieldId,
        int oldTag,
        int newTag,
        FieldChangeKind kind
) implements ValueObject {

}

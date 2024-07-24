package org.metavm.ddl;

import org.metavm.api.EntityType;
import org.metavm.api.Value;

@EntityType
public record FieldChange(
        String klassId,
        String fieldId,
        int oldTag,
        int newTag,
        FieldChangeKind kind
) implements Value {

}

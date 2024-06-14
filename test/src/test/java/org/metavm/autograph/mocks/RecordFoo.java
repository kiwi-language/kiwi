package org.metavm.autograph.mocks;

import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType
public record RecordFoo(
        @EntityField(unique = true) int id,
        @Nullable String name
) {

}

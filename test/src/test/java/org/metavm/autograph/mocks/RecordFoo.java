package org.metavm.autograph.mocks;

import org.metavm.api.EntityField;
import org.metavm.api.EntityType;

import javax.annotation.Nullable;

@EntityType
public record RecordFoo(
        @EntityField(unique = true) int id,
        @Nullable String name
) {

}

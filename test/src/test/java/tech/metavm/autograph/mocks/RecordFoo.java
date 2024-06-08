package tech.metavm.autograph.mocks;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType
public record RecordFoo(
        @EntityField(unique = true) int id,
        @Nullable String name
) {

}

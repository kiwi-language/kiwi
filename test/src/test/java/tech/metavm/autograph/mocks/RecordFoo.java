package tech.metavm.autograph.mocks;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType("记录傻")
public record RecordFoo(
        @EntityField(value = "id", unique = true) int id,
        @Nullable @EntityField("名称") String name
) {

}

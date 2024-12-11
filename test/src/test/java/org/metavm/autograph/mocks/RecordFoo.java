package org.metavm.autograph.mocks;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;

import javax.annotation.Nullable;

@Entity
public record RecordFoo(
        @EntityField(unique = true) int id,
        @Nullable String name
) {

}

package org.metavm.autograph.mocks;

import org.metavm.api.Entity;
import org.metavm.api.EntityField;

import javax.annotation.Nullable;

@Entity
public record RecordFoo(
        @EntityField int id,
        @Nullable String name
) {

}

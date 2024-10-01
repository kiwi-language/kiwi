package org.metavm.entity;

import org.metavm.object.type.Klass;
import org.metavm.object.type.StaticFieldTable;

import javax.annotation.Nullable;

public record ReflectDefineResult(
        Klass klass,
        @Nullable StaticFieldTable staticFieldTable
        ) {
}

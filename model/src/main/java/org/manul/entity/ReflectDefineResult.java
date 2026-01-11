package org.manul.entity;

import org.manul.object.type.Klass;
import org.manul.object.type.StaticFieldTable;

import javax.annotation.Nullable;

public record ReflectDefineResult(
        Klass klass,
        @Nullable StaticFieldTable staticFieldTable
        ) {
}

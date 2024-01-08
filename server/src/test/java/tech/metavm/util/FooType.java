package tech.metavm.util;

import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;

public record FooType(
        ClassType fooType,
        ClassType barType,
        ArrayType barChildArrayType,
        Field fooNameField,
        Field fooBarsField,
        Field barCodeField
) {
}

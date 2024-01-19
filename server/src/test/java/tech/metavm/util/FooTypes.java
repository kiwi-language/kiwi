package tech.metavm.util;

import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;

public record FooTypes(
        ClassType fooType,
        ClassType barType,
        ClassType bazType,
        ArrayType barChildArrayType,
        Field fooNameField,
        Field fooBarsField,
        Field barCodeField,
        Field bazBarField
) {
}

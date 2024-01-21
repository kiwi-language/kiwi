package tech.metavm.util;

import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;

public record FooTypes(
        ClassType fooType,
        ClassType barType,
        ClassType quxType,
        ClassType bazType,
        ArrayType barArrayType,
        ArrayType barChildArrayType,
        ArrayType bazArrayType,
        Field fooNameField,
        Field fooCodeField,
        Field fooBarsField,
        Field fooQuxField,
        Field fooBazListField,
        Field barCodeField,
        Field bazBarsField,
        Field quxAmountField
) {
}

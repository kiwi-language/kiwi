package org.metavm.util;

import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;

public record FooTypes(
        Klass fooType,
        Klass barType,
        Klass quxType,
        Klass bazType,
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

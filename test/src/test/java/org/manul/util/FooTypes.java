package org.manul.util;

import org.manul.object.type.ArrayType;
import org.manul.object.type.Field;
import org.manul.object.type.Klass;

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

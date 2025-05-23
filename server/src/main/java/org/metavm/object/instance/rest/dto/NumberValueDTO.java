package org.metavm.object.instance.rest.dto;

public sealed interface NumberValueDTO extends ValueDTO permits DoubleValueDTO, FloatValueDTO, IntegerValueDTO {

    byte byteValue();

    short shortValue();

    int intValue();

    long longValue();

    float floatValue();

    double doubleValue();


}

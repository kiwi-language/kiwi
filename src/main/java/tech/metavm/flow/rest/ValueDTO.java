package tech.metavm.flow.rest;

import tech.metavm.flow.ValueKind;
import tech.metavm.object.meta.rest.dto.TypeDTO;

public record ValueDTO (
        int kind,
        Object value,
        TypeDTO valueType
) {

    public static ValueDTO constValue(Object value) {
        return new ValueDTO(ValueKind.CONSTANT.code(), value, null);
    }

    public static ValueDTO refValue(String ref) {
        return new ValueDTO(ValueKind.REFERENCE.code(), ref, null);
    }

    public static ValueDTO exprValue(String expr) {
        return new ValueDTO(ValueKind.EXPRESSION.code(), expr, null);
    }
}

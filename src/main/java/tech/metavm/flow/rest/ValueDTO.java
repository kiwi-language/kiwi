package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.flow.ValueKind;
import tech.metavm.object.instance.rest.ExpressionFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.PrimitiveFieldValueDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;

public record ValueDTO (
        int kind,
        FieldValueDTO value,
        TypeDTO valueType
) {

    public static ValueDTO constValue(FieldValueDTO value) {
        return new ValueDTO(ValueKind.CONSTANT.code(), value, null);
    }

    public static ValueDTO refValue(String ref) {
        return new ValueDTO(ValueKind.REFERENCE.code(), new ExpressionFieldValueDTO(ref), null);
    }

    public static ValueDTO exprValue(String expr) {
        return new ValueDTO(ValueKind.EXPRESSION.code(), new ExpressionFieldValueDTO(expr), null);
    }

    @JsonIgnore
    public boolean isNull() {
        if(value == null) {
            return true;
        }
        if(value instanceof PrimitiveFieldValueDTO primitiveFieldValue) {
            return primitiveFieldValue.getValue() == null;
        }
        return false;
    }

}

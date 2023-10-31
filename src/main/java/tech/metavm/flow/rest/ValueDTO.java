package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.flow.ValueKind;
import tech.metavm.object.instance.rest.ExpressionFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;

public record ValueDTO (
        int kind,
        FieldValue value
) {

    public static ValueDTO constValue(FieldValue value) {
        return new ValueDTO(ValueKind.CONSTANT.code(), value);
    }

    public static ValueDTO refValue(String ref) {
        return new ValueDTO(ValueKind.REFERENCE.code(), new ExpressionFieldValueDTO(ref));
    }

    public static ValueDTO exprValue(String expr) {
        return new ValueDTO(ValueKind.EXPRESSION.code(), new ExpressionFieldValueDTO(expr));
    }

    @JsonIgnore
    public boolean isNull() {
        if(value == null) {
            return true;
        }
        if(value instanceof PrimitiveFieldValue primitiveFieldValue) {
            return primitiveFieldValue.getValue() == null;
        }
        return false;
    }

}

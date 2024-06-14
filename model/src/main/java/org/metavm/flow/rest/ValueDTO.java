package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.object.instance.rest.ExpressionFieldValue;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.PrimitiveFieldValue;

public record ValueDTO (
        int kind,
        FieldValue value
) {

    public static ValueDTO constValue(FieldValue value) {
        return new ValueDTO(ValueKindCodes.CONSTANT, value);
    }

    public static ValueDTO refValue(String ref) {
        return new ValueDTO(ValueKindCodes.REFERENCE, new ExpressionFieldValue(ref));
    }

    public static ValueDTO exprValue(String expr) {
        return new ValueDTO(ValueKindCodes.EXPRESSION, new ExpressionFieldValue(expr));
    }

    @JsonIgnore
    public boolean isNull() {
        if(value == null)
            return true;
        if(value instanceof PrimitiveFieldValue primitiveFieldValue)
            return primitiveFieldValue.getValue() == null;
        return false;
    }

}

package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.rest.ExpressionFieldValue;
import tech.metavm.object.instance.rest.ReferenceFieldValue;

public class ValueDTOFactory {

    public static ValueDTO createReference(String expression) {
        return new ValueDTO(
                ValueKind.REFERENCE.code(),
                new ExpressionFieldValue(expression)
        );
    }

    public static ValueDTO createExpression(String expression) {
        return new ValueDTO(
                ValueKind.EXPRESSION.code(),
                new ExpressionFieldValue(expression)
        );
    }

    public static ValueDTO createConstant(Object value) {
        if(value instanceof Id id) {
            return new ValueDTO(
                    ValueKind.CONSTANT.code(),
                    ReferenceFieldValue.create(id.toString())
            );
        }
        else {
            return new ValueDTO(
                    ValueKind.CONSTANT.code(),
                    new ExpressionFieldValue(constantToExpression(value))
            );
        }
    }

    private static String constantToExpression(Object value) {
        if(value == null)
            return "null";
        else if (value instanceof String)
            return "\"" + value + "\"";
        else
            return value.toString();
    }

}

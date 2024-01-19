package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.rest.ExpressionFieldValue;

public class ValueDTOFactory {

    public static ValueDTO createRefeference(String expression) {
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

    public static ValueDTO createConstant(String expression) {
        return new ValueDTO(
                ValueKind.CONSTANT.code(),
                new ExpressionFieldValue(expression)
        );
    }

}

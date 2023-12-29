package tech.metavm.flow;

import tech.metavm.expression.ExpressionParser;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.ParsingContext;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;

public class ValueFactory {

    public static Value create(ValueDTO valueDTO, ParsingContext parsingContext) {
        return create(valueDTO, null, parsingContext);
    }

    public static Value create(ValueDTO valueDTO, @Nullable Type assignedType, ParsingContext parsingContext) {
        if(valueDTO == null)
            return Values.nullValue();
        var expression = ExpressionParser.parse(
                Expressions.constantToExpression(valueDTO.value()),
                assignedType,
                parsingContext
        );
        return ValueKind.getByCode(valueDTO.kind()).createValue(expression);
    }

}

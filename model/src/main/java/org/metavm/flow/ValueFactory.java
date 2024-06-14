package org.metavm.flow;

import org.metavm.expression.ExpressionParser;
import org.metavm.expression.Expressions;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.type.Type;

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

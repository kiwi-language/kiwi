package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.ParsingContext;

public class ValueFactory {

    public static Value getValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        if(valueDTO == null) {
            return null;
        }
        if(valueDTO.type() == ValueType.CONSTANT.code()) {
            return new ConstantValue(valueDTO);
        }
        else if(valueDTO.type() == ValueType.REFERENCE.code()) {
            return new ReferenceValue(valueDTO, parsingContext);
        }
        else if(valueDTO.type() == ValueType.EXPRESSION.code()) {
            return new ExpressionValue(valueDTO, parsingContext);
        }
        throw new IllegalArgumentException("Unsupported value category: " + valueDTO.type());
    }

}

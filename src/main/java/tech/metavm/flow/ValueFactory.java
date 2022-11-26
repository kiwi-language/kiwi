package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.ParsingContext;

public class ValueFactory {

    public static Value getValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        if(valueDTO == null) {
            return null;
        }
        if(valueDTO.kind() == ValueKind.CONSTANT.code()) {
            return new ConstantValue(valueDTO/*, parsingContext.getInstanceContext()*/);
        }
        else if(valueDTO.kind() == ValueKind.REFERENCE.code()) {
            return new ReferenceValue(valueDTO, parsingContext);
        }
        else if(valueDTO.kind() == ValueKind.EXPRESSION.code()) {
            return new ExpressionValue(valueDTO, parsingContext);
        }
        throw new IllegalArgumentException("Unsupported value category: " + valueDTO.kind());
    }

}

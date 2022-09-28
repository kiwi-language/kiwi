package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;

public class ValueFactory {

    public static Value getValue(ValueDTO valueDTO) {
        if(valueDTO == null) {
            return null;
        }
        if(valueDTO.type() == ValueType.CONSTANT.code()) {
            return new ConstantValue(valueDTO);
        }
        else if(valueDTO.type() == ValueType.REFERENCE.code()) {
            return new ReferenceValue(valueDTO);
        }
        else if(valueDTO.type() == ValueType.EXPRESSION.code()) {
            return new ExpressionValue(valueDTO);
        }
        throw new IllegalArgumentException("Unsupported value category: " + valueDTO.type());
    }

}

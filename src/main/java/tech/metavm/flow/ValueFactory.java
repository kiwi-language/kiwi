package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.ParsingContext;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;

import javax.annotation.Nullable;

public class ValueFactory {

    public static Value create(ValueDTO valueDTO, ParsingContext parsingContext) {
        return create(valueDTO, null, parsingContext);
    }

    public static Value create(ValueDTO valueDTO, @Nullable Type assignedType, ParsingContext parsingContext) {
        if(valueDTO == null) {
            return new ConstantValue(ExpressionUtil.constant(InstanceUtils.nullInstance()));
        }
        if(valueDTO.kind() == ValueKind.CONSTANT.code()) {
            return new ConstantValue(valueDTO, assignedType, parsingContext);
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

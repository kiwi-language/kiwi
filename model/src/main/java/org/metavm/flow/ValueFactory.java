package org.metavm.flow;

import org.metavm.expression.ExpressionParser;
import org.metavm.expression.FlowParsingContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public class ValueFactory {

    public static Value create(ValueDTO valueDTO, ParsingContext parsingContext) {
        return create(valueDTO, null, parsingContext);
    }

    public static Value create(ValueDTO valueDTO, @Nullable Type assignedType, ParsingContext parsingContext) {
        if(valueDTO == null)
            return Values.nullValue();
        return switch (valueDTO) {
            case ConstantValueDTO constantValueDTO -> new ConstantValue(
                    Instances.fromFieldValue(constantValueDTO.value(), parsingContext::getInstance)
            );
            case TypeValueDTO typeValueDTO ->
                new TypeValue(ExpressionParser.parseType(typeValueDTO.type(), parsingContext));
            case ArrayValueDTO arrayValueDTO ->
                new ArrayValue(
                        NncUtils.map(arrayValueDTO.elements(), e -> create(e, null, parsingContext)),
                        (ArrayType) ExpressionParser.parseType(arrayValueDTO.type(), parsingContext)
                );
            case NeverValueDTO neverValueDTO -> new NeverValue();
            case NodeValueDTO nodeValueDTO -> new NodeValue(
                    ((FlowParsingContext) parsingContext).getNodeById(Id.parse(nodeValueDTO.nodeId()))
            );
            case PropertyValueDTO propertyValueDTO -> new PropertyValue(
                    PropertyRef.create(propertyValueDTO.propertyRef(), parsingContext.getTypeDefProvider())
            );
            case ExpressionValueDTO expressionValueDTO -> {
                var expression = ExpressionParser.parse(
                        expressionValueDTO.expression(),
                        assignedType,
                        parsingContext
                );
                yield new ExpressionValue(expression);
            }
            default -> throw new IllegalStateException("Unrecognized ValueDTO: " + valueDTO);
        };
    }

}

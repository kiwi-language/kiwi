package tech.metavm.flow;

import tech.metavm.entity.ValueType;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.*;
import tech.metavm.object.meta.Type;

@ValueType("引用值")
public class ReferenceValue extends Value {

//    private final String nodeId;
//    private final String fieldId;
    private final Expression expression;

    public ReferenceValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        super(valueDTO/*, parsingContext.getInstanceContext()*/);
        String value = (String) valueDTO.value();
//        if(value.contains(".")) {
//            String[] splits = value.split("\\.");
//            nodeId = splits[0];
//            fieldId = splits[1];
            expression = ExpressionParser.parse(value, parsingContext);
//        }
//        else {
//            nodeId = value;
//            fieldId = null;
//            expression = ExpressionParser.parse(value, parsingContext);
//        }
    }

    @Override
    protected Object getDTOValue(boolean persisting) {
        return expression.buildSelf(persisting ? VarType.ID : VarType.NAME);
//        if(fieldId != null) {
//            return nodeId + "." + fieldId;
//        }
//        else {
//            return nodeId + "";
//        }
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        return ExpressionEvaluator.evaluate(expression, context);
    }

}

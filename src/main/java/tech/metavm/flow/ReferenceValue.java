package tech.metavm.flow;

import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.query.*;

public class ReferenceValue extends Value {

    private final long nodeId;
    private final long fieldId;
    private final transient Expression expression;

    public ReferenceValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        super(valueDTO);
        String value = (String) valueDTO.value();
        if(value.contains("-")) {
            String[] splits = value.split("-");
            nodeId = Long.parseLong(splits[0]);
            fieldId = Long.parseLong(splits[1]);
            expression = parsingContext.parse(Var.idVars(nodeId, fieldId));
        }
        else {
            nodeId = Long.parseLong(value);
            fieldId = -1L;
            expression = parsingContext.parse(Var.idVars(nodeId));
        }
    }

    @Override
    protected Object getDTOValue(boolean persisting) {
        if(fieldId >= 0L) {
            return nodeId + "-" + fieldId;
        }
        else {
            return nodeId + "";
        }
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        return ExpressionEvaluator.evaluate(expression, context);
    }

}

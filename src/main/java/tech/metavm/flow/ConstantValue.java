package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.*;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.PrimitiveFieldValueDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InstanceUtils;

@EntityType("常量值")
public class ConstantValue extends Value {

    @ChildEntity("值")
    private final Expression expression;

    public ConstantValue(ValueDTO valueDTO, ParsingContext parsingContext) {
        super(ValueKind.CONSTANT);
        if(valueDTO.isNull()) {
            expression = ExpressionUtil.constant(InstanceUtils.nullInstance());
        }
        else {
            expression = ExpressionParser.parse(
                    ExpressionUtil.constantToExpression(valueDTO.value()),
                    parsingContext
            );
        }
    }

    public ConstantValue(Expression expression) {
        super(ValueKind.CONSTANT);
        this.expression = expression;
    }

    @Override
    protected FieldValueDTO getDTOValue(boolean persisting) {
        ConstantExpression constantExpression = (ConstantExpression) expression;
        return ExpressionUtil.expressionToConstant(constantExpression);
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        return ExpressionEvaluator.evaluate(expression, context);
    }

}

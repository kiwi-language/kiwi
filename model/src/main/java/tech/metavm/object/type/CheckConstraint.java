package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.expression.*;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.flow.Value;
import tech.metavm.flow.ValueFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.Instances;

import javax.annotation.Nullable;

@EntityType("校验约束")
public class CheckConstraint extends Constraint {

    @ChildEntity("条件")
    private Value condition;

    private transient ConstraintDef<?> constraintDef;

    public CheckConstraint(ClassType type, String name, @Nullable String code, String message, Value condition) {
        super(ConstraintKind.CHECK, type, name, code, message);
        setCondition(condition);
    }

    @Override
    public String getDefaultMessage() {
        return "校验规则校验不通过";
    }

    @Override
    protected CheckConstraintParam getParam() {
        return new CheckConstraintParam(
                condition.toDTO()
        );
    }

    @Override
    public void setParam(Object param, IEntityContext context) {
        var checkParam = (CheckConstraintParam) param;
        var parsingContext = getParsingContext(context);
        if(checkParam.value() != null)
            setCondition(ValueFactory.create(checkParam.value(), parsingContext));
    }

    public boolean isFieldConstraint(Field field) {
        if(condition.getExpression() instanceof BinaryExpression binExpr
                && binExpr.getLeft() instanceof PropertyExpression propExpr) {
            return propExpr.getProperty() == field;
        }
        else if(condition.getExpression() instanceof UnaryExpression unaryExpr
                && unaryExpr.getOperand() instanceof PropertyExpression propExr) {
            return propExr.getProperty() == field;
        }
        else {
            return false;
        }
    }

    @Override
    public String getDesc() {
        return "";
    }

    public void setCondition(Value condition) {
        this.condition = addChild(condition, "condition");
    }

    public Value getCondition() {
        return condition;
    }

    public boolean check(ClassInstance instance, ParameterizedFlowProvider parameterizedFlowProvider) {
        return Instances.isTrue(condition.evaluate(new InstanceEvaluationContext(instance, parameterizedFlowProvider)));
    }

    public ConstraintDef<?> getConstraintDef() {
        return constraintDef;
    }

    public void setConstraintDef(ConstraintDef<?> constraintDef) {
        this.constraintDef = constraintDef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCheckConstraint(this);
    }
}

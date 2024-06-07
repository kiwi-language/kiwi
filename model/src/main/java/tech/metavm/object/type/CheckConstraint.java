package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.expression.BinaryExpression;
import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.expression.PropertyExpression;
import tech.metavm.expression.UnaryExpression;
import tech.metavm.flow.Value;
import tech.metavm.flow.ValueFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.Instances;

import javax.annotation.Nullable;

@EntityType
public class CheckConstraint extends Constraint {

    private Value condition;

    private transient ConstraintDef<?> constraintDef;

    public CheckConstraint(Klass type, String name, @Nullable String code, String message, Value condition) {
        super(ConstraintKind.CHECK, type, name, code, message);
        this.condition = condition;
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
        this.condition = condition;
    }

    public Value getCondition() {
        return condition;
    }

    public boolean check(ClassInstance instance) {
        return Instances.isTrue(condition.evaluate(new InstanceEvaluationContext(instance)));
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

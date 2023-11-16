package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.expression.*;
import tech.metavm.flow.Value;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.InstanceUtils;

@EntityType("校验约束")
public class CheckConstraint extends Constraint {

    @EntityField("条件")
    private Value condition;

    private transient ConstraintDef<?> constraintDef;

    public CheckConstraint(Value condition, ClassType type, String message) {
        super(ConstraintKind.CHECK, type, message);
        setCondition(condition);
    }

    @Override
    public String getDefaultMessage() {
        return "校验规则校验不通过";
    }

    @Override
    protected CheckConstraintParamDTO getParam(boolean forPersistence) {
        return new CheckConstraintParamDTO(
                condition.toDTO(forPersistence)
        );
    }

    public boolean isFieldConstraint(Field field) {
        if(condition.getExpression() instanceof BinaryExpression binExpr
                && binExpr.getFirst() instanceof PropertyExpression propExpr) {
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

    public boolean check(ClassInstance instance, IEntityContext entityContext) {
        return InstanceUtils.isTrue(condition.evaluate(new InstanceEvaluationContext(instance, entityContext)));
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

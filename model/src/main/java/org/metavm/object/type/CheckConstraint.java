package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.ConstraintDef;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.expression.BinaryExpression;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.UnaryExpression;
import org.metavm.flow.Value;
import org.metavm.flow.ValueFactory;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.rest.dto.CheckConstraintParam;
import org.metavm.util.Instances;

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
        return "Constraint check failed";
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

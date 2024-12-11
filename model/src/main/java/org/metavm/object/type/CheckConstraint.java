package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ConstraintDef;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.BinaryExpression;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.UnaryExpression;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.flow.Value;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.util.Instances;

@Entity
public class CheckConstraint extends Constraint {

    private Value condition;

    private transient ConstraintDef<?> constraintDef;

    public CheckConstraint(Klass type, String name, String message, Value condition) {
        super(type, name, message);
        this.condition = condition;
    }

    @Override
    public String getDefaultMessage() {
        return "Constraint check failed";
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

    @Override
    public void read(KlassInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(KlassOutput output) {
        throw new UnsupportedOperationException();
    }
}

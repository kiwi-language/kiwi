package org.metavm.object.type;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.common.ErrorCode;
import org.metavm.util.BusinessException;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.BinaryExpression;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.UnaryExpression;
import org.metavm.flow.Value;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;

import java.util.function.Consumer;

@Getter
@Wire(32)
@Entity
public class CheckConstraint extends Constraint {

    private final Value condition;

    public CheckConstraint(@NotNull Id id, Klass type, String name, String message, Value condition) {
        super(id, type, name, message);
        this.condition = condition;
    }

    public static BusinessException constraintCheckFailed(Instance instance, Constraint constraint) {
        String reason = constraint.getMessage() != null ? constraint.getMessage() : constraint.getDefaultMessage();
        throw new BusinessException(
                ErrorCode.CONSTRAINT_CHECK_FAILED,
                instance.getTitle(),
                reason
        );
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

    public boolean check(ClassInstance instance) {
        return Instances.isTrue(condition.evaluate(new InstanceEvaluationContext(instance)));
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCheckConstraint(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        condition.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}

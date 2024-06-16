package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public class AllMatchExpression extends Expression {

    private final Expression array;
    private final Expression condition;

    public AllMatchExpression(@NotNull Expression array, @NotNull Expression condition) {
        this.array = array;
        this.condition = condition;
    }

    public Expression getArray() {
        return array;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "allmatch(" +
                getArray().build(symbolType, false, relaxedCheck) + ", "
                + condition.build(symbolType, false, relaxedCheck)
                + ")";
    }

    public ArrayType getArrayType() {
        return (ArrayType) getArray().getType();
    }

    @Override
    public int precedence() {
        return 10;
    }

    @Override
    public Type getType() {
        return new PrimitiveType(PrimitiveKind.BOOLEAN);
    }

    public @Nullable CursorExpression createCursor() {
        return new CursorExpression(getArrayType().getElementType(), getCursorAlias());
    }

    @Nullable
    public String getCursorAlias() {
        return array instanceof AsExpression asExpression ? asExpression.getAlias() : null;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(array, condition);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        Instance instance = array.evaluate(context);
        if (instance.isNull()) {
            return Instances.trueInstance();
        }
        if (!(instance instanceof ArrayInstance arrayInst)) {
            throw new InternalException("Expecting array instance for AllMatchExpression but got " + instance);
        }
        for (Instance element : arrayInst.getElements()) {
            if (element instanceof ClassInstance classInstance) {
                EvaluationContext subContext = new SubEvaluationContext(context, classInstance);
                if (!Instances.isTrue(condition.evaluate(subContext))) {
                    return Instances.falseInstance();
                }
            } else {
                throw new InternalException("AllMatchExpression only supports reference array right now");
            }
        }
        return Instances.trueInstance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllMatchExpression that)) return false;
        return Objects.equals(array, that.array) && Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, condition);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAllMatchExpression(this);
    }
}

package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static tech.metavm.util.InstanceUtils.trueInstance;

@EntityType("AllMatch表达式")
public class AllMatchExpression extends Expression {

    //    private final CursorExpression cursor;
    @ChildEntity("数组")
    private final Expression array;
    @ChildEntity("条件")
    private final Expression condition;
    @Nullable
    @EntityField("游标")
    private final CursorExpression cursor;

    public AllMatchExpression(@NotNull Expression array, @NotNull Expression condition) {
        this(array, condition, null);
    }

    public AllMatchExpression(@NotNull Expression array, @NotNull Expression condition, @Nullable CursorExpression cursor) {
        this.array = addChild(array.copy(), "array");
        this.condition = addChild(condition.copy(), "condition");
        this.cursor = cursor;
    }

    public Expression getArray() {
        return array;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return "allmatch(" +
                getArray().build(symbolType, false) + ", "
                + condition.build(symbolType, false)
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
        return ModelDefRegistry.getType(Boolean.class);
    }

    public CursorExpression getCursor() {
        return cursor;
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
    public Expression substituteChildren(List<Expression> children) {
        NncUtils.requireLength(children, 2);
        return new AllMatchExpression(children.get(0), children.get(1), null);
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        Instance instance = array.evaluate(context);
        if (instance.isNull()) {
            return InstanceUtils.trueInstance();
        }
        if (!(instance instanceof ArrayInstance arrayInst)) {
            throw new InternalException("Expecting array instance for AllMatchExpression but got " + instance);
        }
        for (Instance element : arrayInst.getElements()) {
            if (element instanceof ClassInstance classInstance) {
                EvaluationContext subContext = new SubEvaluationContext(context, cursor, classInstance);
                if (!InstanceUtils.isTrue(
                        condition.evaluate(subContext))
                ) {
                    return InstanceUtils.falseInstance();
                }
            } else {
                throw new InternalException("AllMatchExpression only supports reference array right now");
            }
        }
        return InstanceUtils.trueInstance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllMatchExpression that)) return false;
        return Objects.equals(array, that.array) && Objects.equals(condition, that.condition) && Objects.equals(cursor, that.cursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, condition, cursor);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAllMatchExpression(this);
    }
}

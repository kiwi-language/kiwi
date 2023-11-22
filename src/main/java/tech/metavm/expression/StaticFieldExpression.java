package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("静态字段表达式")
public class StaticFieldExpression extends Expression {

    @EntityField("字段")
    private final Field field;

    public StaticFieldExpression(@NotNull Field field) {
        this.field = field;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        try(var context = SerializeContext.enter()) {
            if(context.isIncludeExpressionType()) {
                context.writeType(field.getDeclaringType());
            }
            return switch (symbolType) {
                case NAME -> field.getDeclaringType().getName() + "." + field.getName();
                case ID -> idVarName(field.getDeclaringType().getIdRequired()) + "." +
                        idVarName(field.getIdRequired());
            };
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return field.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    public Field getField() {
        return field;
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        return new StaticFieldExpression(field);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticFieldExpression that)) return false;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStaticFieldExpression(this);
    }
}

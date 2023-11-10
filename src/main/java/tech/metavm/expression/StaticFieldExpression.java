package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Objects;

public class StaticFieldExpression extends Expression {

    private final Field field;

    public StaticFieldExpression(Field field) {
        this.field = field;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return switch (symbolType) {
            case NAME -> field.getDeclaringType().getName() + "." + field.getName();
            case ID -> idVarName(field.getDeclaringType().getIdRequired()) + "." +
                    idVarName(field.getIdRequired());
        };
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

package tech.metavm.expression;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("字段表达式")
public class FieldExpression extends Expression {

    @ChildEntity("对象")
    private final Expression instance;

    @EntityField("字段")
    private final Field field;

    public FieldExpression(Expression instance, Field field) {
        this.instance = requireNonNull(instance);
        this.field = requireNonNull(field);
    }

    public Field getField() {
        return field;
    }

    @Override
    public Type getType() {
        return field.getType();
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return new FieldExpression(instance, field);
    }

    @Override
    public String buildSelf(VarType symbolType) {
        String fieldsExpr = switch (symbolType) {
            case ID -> idVarName(field.getIdRequired());
            case NAME -> field.getName();
        };
        if((instance instanceof CursorExpression cursorExpression) && cursorExpression.getAlias() == null) {
            return fieldsExpr;
        }
        else {
            String instanceExpr = instance.build(symbolType, instance.precedence() > precedence());
            return instanceExpr + "." + fieldsExpr;
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    public Expression getInstance() {
        return instance;
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return super.extractExpressionsRecursively(klass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldExpression that)) return false;
        return Objects.equals(instance, that.instance) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, field);
    }
}

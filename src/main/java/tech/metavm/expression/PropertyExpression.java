package tech.metavm.expression;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.Property;
import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("属性表达式")
public class PropertyExpression extends Expression {

    @ChildEntity("对象")
    private final Expression instance;

    @EntityField("属性")
    private final Property property;

    public PropertyExpression(Expression instance, Property attribute) {
        this.instance = requireNonNull(instance);
        this.property = requireNonNull(attribute);
    }

    public Property getProperty() {
        return property;
    }

    @Override
    public Type getType() {
        return property.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        return new PropertyExpression(instance, property);
    }

    @Override
    public String buildSelf(VarType symbolType) {
        String fieldsExpr = switch (symbolType) {
            case ID -> idVarName(property.getIdRequired());
            case NAME -> property.getName();
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
        if (!(o instanceof PropertyExpression that)) return false;
        return Objects.equals(instance, that.instance) && Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, property);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPropertyExpression(this);
    }
}

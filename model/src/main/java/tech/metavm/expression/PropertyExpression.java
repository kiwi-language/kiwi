package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Property;
import tech.metavm.object.type.PropertyRef;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("属性表达式")
public class PropertyExpression extends Expression {

    @ChildEntity("对象")
    private final Expression instance;

    @ChildEntity("属性")
    private final PropertyRef propertyRef;

    public PropertyExpression(@NotNull Expression instance, @NotNull PropertyRef propertyRef) {
        this.instance = addChild(instance.copy(), "instance");
        this.propertyRef = addChild((Entity & PropertyRef) propertyRef.copy(), "propertyRef");
    }

    public Property getProperty() {
        return propertyRef.resolve();
    }

    @Override
    public Type getType() {
        return getProperty().getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(instance);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return ((ClassInstance) instance.evaluate(context)).getProperty(getProperty());
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        String fieldsExpr = switch (symbolType) {
            case ID -> idVarName(requireNonNull(getProperty().tryGetId()));
            case NAME -> getProperty().getName();
        };
        if((instance instanceof CursorExpression cursorExpression) && cursorExpression.getAlias() == null) {
            return fieldsExpr;
        }
        else {
            String instanceExpr = instance.build(symbolType, instance.precedence() > precedence(), relaxedCheck);
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
        return Objects.equals(instance, that.instance) && Objects.equals(propertyRef, that.propertyRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, propertyRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPropertyExpression(this);
    }
}

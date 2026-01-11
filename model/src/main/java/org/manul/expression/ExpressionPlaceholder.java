package org.manul.expression;

import org.manul.api.Entity;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.Value;
import org.manul.object.type.NeverType;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

import java.util.List;
import java.util.function.Consumer;

@Wire
@Entity
public class ExpressionPlaceholder extends Expression {

    @Generated
    public static ExpressionPlaceholder read(MvInput input) {
        return new ExpressionPlaceholder();
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return NeverType.instance;
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitExpressionPlaceholder(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ExpressionPlaceholder);
        super.write(output);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new ExpressionPlaceholder();
    }
}

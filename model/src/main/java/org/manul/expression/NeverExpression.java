package org.manul.expression;

import org.manul.api.Generated;
import org.manul.entity.ElementVisitor;
import org.manul.entity.StdKlassRegistry;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.Value;
import org.manul.object.type.Type;
import org.manul.object.type.Types;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;
import org.manul.wire.Wire;

import java.util.List;
import java.util.function.Consumer;

@Wire
public class NeverExpression extends Expression {
    public static final org.manul.object.type.Klass __klass__ = StdKlassRegistry.instance.getKlass(NeverExpression.class);

    @Generated
    public static NeverExpression read(MvInput input) {
        return new NeverExpression();
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "never";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return Types.getNeverType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new IllegalStateException("NeverExpression should not be evaluated");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeverExpression(this);
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
        output.write(TYPE_NeverExpression);
        super.write(output);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new NeverExpression();
    }
}

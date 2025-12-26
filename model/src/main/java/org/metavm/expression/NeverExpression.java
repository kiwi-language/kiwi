package org.metavm.expression;

import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlassRegistry;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.wire.Wire;

import java.util.List;
import java.util.function.Consumer;

@Wire
public class NeverExpression extends Expression {
    public static final org.metavm.object.type.Klass __klass__ = StdKlassRegistry.instance.getKlass(NeverExpression.class);

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

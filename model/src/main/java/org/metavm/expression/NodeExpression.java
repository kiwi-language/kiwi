package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Node;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire
@Entity
public class NodeExpression extends Expression {

    private final Reference node;

    public NodeExpression(@NotNull Node node) {
        this.node = node.getReference();
    }

    public NodeExpression(Reference node) {
        this.node = node;
    }

    @Generated
    public static NodeExpression read(MvInput input) {
        return new NodeExpression((Reference) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    public Node getNode() {
        return (Node) node.get();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return switch (symbolType) {
            case ID -> {
                try(var serContext = SerializeContext.enter()) {
                    yield idVarName(serContext.getId(node));
                }
            }
            case NAME -> getNode().getName();
        };
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return getNode().getType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return context.evaluate(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeExpression that)) return false;
        return Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNodeExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(node);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_NodeExpression);
        super.write(output);
        output.writeValue(node);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new NodeExpression(node);
    }
}

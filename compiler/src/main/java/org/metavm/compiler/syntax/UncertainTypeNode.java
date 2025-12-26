package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;

import java.util.Objects;
import java.util.function.Consumer;

public final class UncertainTypeNode extends TypeNode {
    private final TypeNode lowerBound;
    private final TypeNode upperBound;

    public UncertainTypeNode(TypeNode lowerBound, TypeNode upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("[");
        lowerBound.write(writer);
        writer.write(", ");
        upperBound.write(writer);
        writer.write("]");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitUncertainTypeNode(this);
    }
    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(lowerBound);
        action.accept(upperBound);
    }

    public TypeNode lowerBound() {
        return lowerBound;
    }

    public TypeNode upperBound() {
        return upperBound;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UncertainTypeNode) obj;
        return Objects.equals(this.lowerBound, that.lowerBound) &&
                Objects.equals(this.upperBound, that.upperBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    @Override
    public String toString() {
        return "UncertainTypeNode[" +
                "lowerBound=" + lowerBound + ", " +
                "upperBound=" + upperBound + ']';
    }

    @Override
    protected Type actualResolve(Env env) {
        return env.types().getUncertainType(
                env.resolveType(lowerBound),
                env.resolveType(upperBound)
        );
    }
}

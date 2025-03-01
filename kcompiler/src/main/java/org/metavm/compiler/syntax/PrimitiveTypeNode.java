package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;

import java.util.Objects;
import java.util.function.Consumer;

public final class PrimitiveTypeNode extends TypeNode {
    private final TypeTag tag;

    public PrimitiveTypeNode(TypeTag tag) {
        this.tag = tag;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(tag.name().toLowerCase());
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitPrimitiveTypeNode(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {

    }

    public TypeTag tag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PrimitiveTypeNode) obj;
        return Objects.equals(this.tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public String toString() {
        return "PrimitiveTypeNode[" +
                "tag=" + tag + ']';
    }

    @Override
    protected Type actualResolve(Env env) {
        return PrimitiveType.valueOf(tag.name());
    }
}

package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.ArrayType;
import org.metavm.compiler.type.Type;

import java.util.Objects;
import java.util.function.Consumer;

public final class ArrayTypeNode extends TypeNode {
    private final TypeNode element;

    public ArrayTypeNode(TypeNode element) {
        this.element = element;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(element);
        writer.write("[]");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitArrayTypeNode(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(element);
    }

    public TypeNode element() {
        return element;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ArrayTypeNode) obj;
        return Objects.equals(this.element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    protected Type actualResolve(Env env) {
        return env.types().getArrayType(env.resolveType(element));
    }
}

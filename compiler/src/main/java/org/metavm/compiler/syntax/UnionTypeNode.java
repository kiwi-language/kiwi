package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

public final class UnionTypeNode extends TypeNode {
    private final List<TypeNode> alternatives;

    public UnionTypeNode(List<TypeNode> alternatives) {
        this.alternatives = alternatives;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(alternatives, "|");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitUnionTypeNode(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        alternatives.forEach(action);
    }

    public List<TypeNode> alternatives() {
        return alternatives;
    }

    @Override
    protected Type actualResolve(Env env) {
        return env.types().getUnionType(alternatives.map(env::resolveType));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UnionTypeNode that = (UnionTypeNode) object;
        return Objects.equals(alternatives, that.alternatives);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alternatives);
    }
}

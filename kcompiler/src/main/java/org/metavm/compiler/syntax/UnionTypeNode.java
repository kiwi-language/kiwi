package org.metavm.compiler.syntax;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.UnionType;
import org.metavm.compiler.util.List;

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
    public String toString() {
        return "UnionTypeNode[" +
                "alternatives=" + alternatives + ']';
    }

    @Override
    protected Type actualResolve(Env env) {
        return env.types().getUnionType(alternatives.map(env::resolveType));
    }
}

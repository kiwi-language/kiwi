package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import java.util.function.Consumer;

@Slf4j
public final class IntersectionTypeNode extends TypeNode {
    private final List<TypeNode> bounds;

    public IntersectionTypeNode(List<TypeNode> bounds) {
        this.bounds = bounds;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(bounds, " & ");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitIntersectionTypeNode(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        bounds.forEach(action);
    }

    public List<TypeNode> bounds() {
        return bounds;
    }

    @Override
    public String toString() {
        return "IntersectionTypeNode[" +
                "bounds=" + bounds + ']';
    }

    @Override
    protected Type actualResolve(Env env) {
        return env.types().getIntersectionType(bounds.map(env::resolveType));
    }
}

package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import java.util.Objects;
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
    protected Type actualResolve(Env env) {
        return env.types().getIntersectionType(bounds.map(env::resolveType));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        IntersectionTypeNode that = (IntersectionTypeNode) object;
        return Objects.equals(bounds, that.bounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bounds);
    }
}

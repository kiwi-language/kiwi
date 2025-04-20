package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public final class FunctionTypeNode extends TypeNode {
    private final List<TypeNode> parameterTypes;
    private final TypeNode returnType;

    public FunctionTypeNode(List<TypeNode> parameterTypes, TypeNode returnType) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("(");
        writer.write(parameterTypes);
        writer.write(") -> ");
        writer.write(returnType);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitFunctionTypeNode(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        parameterTypes.forEach(action);
        action.accept(returnType);
    }

    public List<TypeNode> parameterTypes() {
        return parameterTypes;
    }

    public TypeNode returnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FunctionTypeNode) obj;
        return Objects.equals(this.parameterTypes, that.parameterTypes) &&
                Objects.equals(this.returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterTypes, returnType);
    }

    @Override
    public String toString() {
        return "FunctionTypeNode[" +
                "parameterTypes=" + parameterTypes + ", " +
                "returnType=" + returnType + ']';
    }

    @Override
    protected Type actualResolve(Env env) {
        return env.types().getFuncType(
                parameterTypes.map(env::resolveType),
                env.resolveType(returnType)
        );
    }
}

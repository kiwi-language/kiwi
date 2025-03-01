package org.metavm.compiler.syntax;


import org.metavm.compiler.util.List;

import java.util.Objects;
import java.util.function.Consumer;

public final class TypeApply extends Node {
    private final Name clazz;
    private final List<TypeNode> typeArguments;

    public TypeApply(Name clazz, List<TypeNode> typeArguments) {
        this.clazz = clazz;
        this.typeArguments = typeArguments;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(clazz);
        writer.write("<");
        writer.write(typeArguments);
        writer.write(">");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitTypeApply(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(clazz);
        typeArguments.forEach(action);
    }

    public Name clazz() {
        return clazz;
    }

    public List<TypeNode> typeArguments() {
        return typeArguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TypeApply) obj;
        return Objects.equals(this.clazz, that.clazz) &&
                Objects.equals(this.typeArguments, that.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, typeArguments);
    }

    @Override
    public String toString() {
        return "ParameterizedTypeNode[" +
                "clazz=" + clazz + ", " +
                "typeArguments=" + typeArguments + ']';
    }

}

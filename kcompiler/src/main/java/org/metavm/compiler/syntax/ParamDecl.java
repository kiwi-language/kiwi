package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Parameter;

import java.util.Objects;
import java.util.function.Consumer;

public final class ParamDecl extends Decl<Parameter> {
    private final TypeNode type;
    private final Ident name;

    public ParamDecl(TypeNode type, Ident name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public void write(SyntaxWriter writer) {
        name.write(writer);
        writer.write(": ");
        type.write(writer);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitParamDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(type);
        action.accept(name);
    }

    public TypeNode type() {
        return type;
    }

    public Ident name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParamDecl) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "Param[" +
                "type=" + type + ", " +
                "name=" + name + ']';
    }

}

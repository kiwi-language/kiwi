package org.metavm.compiler.syntax;

import org.metavm.compiler.element.LocalVariable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class LocalVarDecl extends Decl<LocalVariable> {
    @Nullable
    private final TypeNode type;
    private final Ident name;
    @Nullable
    private final Expr initial;

    public LocalVarDecl(@Nullable TypeNode type, Ident name, @Nullable Expr initial) {
        this.type = type;
        this.name = name;
        this.initial = initial;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("var ");
        writer.write(name);
        if (type != null) {
            writer.write(": ");
            writer.write(type);
        }
        if (initial != null) {
            writer.write(" = ");
            writer.write(initial);
        }

    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitLocalVarDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        action.accept(name);
        if (type != null)
            action.accept(type);
        if (initial != null)
            action.accept(initial);
    }

    @Nullable
    public TypeNode type() {
        return type;
    }

    public Ident name() {
        return name;
    }

    @Nullable
    public Expr initial() {
        return initial;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LocalVarDecl) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.initial, that.initial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, initial);
    }

    @Override
    public String toString() {
        return "VarDecl[" +
                "type=" + type + ", " +
                "name=" + name + ", " +
                "initial=" + initial + ']';
    }

}

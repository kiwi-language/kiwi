package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.TypeVar;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class TypeVariableDecl extends Decl<TypeVar>  {

    private Name name;
    private @Nullable TypeNode bound;

    public TypeVariableDecl(Name name, @Nullable TypeNode bound) {
        this.name = name;
        this.bound = bound;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    @Nullable
    public TypeNode getBound() {
        return bound;
    }

    public void setBound(@Nullable TypeNode bound) {
        this.bound = bound;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(name);
        if (bound != null) {
            writer.write(": ");
            writer.write(bound);
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitTypeVariableDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        if (bound != null)
            action.accept(bound);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TypeVariableDecl decl = (TypeVariableDecl) object;
        return Objects.equals(name, decl.name) && Objects.equals(bound, decl.bound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bound);
    }

    @Override
    public TypeVariableDecl setPos(int pos) {
        return (TypeVariableDecl) super.setPos(pos);
    }
}

package org.metavm.compiler.syntax;

import org.metavm.compiler.element.TypeVariable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TypeVariableDecl extends Decl<TypeVariable>  {

    private Ident name;
    private @Nullable TypeNode bound;

    public TypeVariableDecl(Ident name, @Nullable TypeNode bound) {
        this.name = name;
        this.bound = bound;
    }

    public Ident getName() {
        return name;
    }

    public void setName(Ident name) {
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
        action.accept(name);
        if (bound != null)
            action.accept(bound);
    }
}

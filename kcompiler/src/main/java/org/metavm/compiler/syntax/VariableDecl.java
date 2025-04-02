package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Variable;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class VariableDecl<T extends Variable> extends Decl<T> {

    @Nullable
    private TypeNode type;
    private final Name name;
    @Nullable
    private Expr initial;

    public VariableDecl(@Nullable TypeNode type, Name name, @Nullable Expr initial) {
        this.type = type;
        this.name = name;
        this.initial = initial;
    }


    @Nullable
    public TypeNode getType() {
        return type;
    }

    public void setType(@Nullable TypeNode type) {
        this.type = type;
    }

    public Name getName() {
        return name;
    }

    @Nullable
    public Expr getInitial() {
        return initial;
    }

    public void setInitial(@Nullable Expr initial) {
        this.initial = initial;
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        if (type != null)
            action.accept(type);
        if (initial != null)
            action.accept(initial);
    }



}

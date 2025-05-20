package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Variable;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class VariableDecl<T extends Variable> extends Decl<T> {

    private List<Annotation> annotations;
    @Nullable
    private TypeNode type;
    private final Name name;
    @Nullable
    private Expr initial;

    public VariableDecl(List<Annotation> annotations, @Nullable TypeNode type, Name name, @Nullable Expr initial) {
        this.annotations = annotations;
        this.type = type;
        this.name = name;
        this.initial = initial;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
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

package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Variable;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class VariableDecl<T extends Variable> extends ModifiedDecl<T> {

    private List<Annotation> annotations;
    @Nullable
    private TypeNode type;
    private final Name name;
    @Nullable
    private Expr initial;
    private final boolean mutable;

    public VariableDecl(List<Annotation> annotations, @Nullable TypeNode type, Name name, @Nullable Expr initial, boolean mutable) {
        this.annotations = annotations;
        this.type = type;
        this.name = name;
        this.initial = initial;
        this.mutable = mutable;
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
        annotations.forEach(action);
        if (type != null)
            action.accept(type);
        if (initial != null)
            action.accept(initial);
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        VariableDecl<?> that = (VariableDecl<?>) object;
        return Objects.equals(annotations, that.annotations) && Objects.equals(type, that.type) && Objects.equals(name, that.name) && Objects.equals(initial, that.initial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotations, type, name, initial);
    }

    public boolean isMutable() {
        return mutable;
    }
}
package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Field;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class FieldDecl extends VariableDecl<Field> {
    private final List<Modifier> mods;

    public FieldDecl(
            List<Modifier> mods,
            List<Annotation> annotations,
            @Nullable TypeNode type,
            Name name,
            @Nullable Expr initial
    ) {
        super(annotations, type, name, initial);
        this.mods = mods;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (!mods.isEmpty()) {
            writer.write(mods, " ");
            writer.write(" ");
        }
        writer.write("var ");
        writer.write(getName());
        if (getType() != null) {
            writer.write(": ");
            writer.write(getType());
        }
        if (getInitial() != null) {
            writer.write(" = ");
            writer.write(getInitial());
        }
        writer.writeln();
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitFieldDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        mods.forEach(action);
        super.forEachChild(action);
    }

    public List<Modifier> getMods() {
        return mods;
    }

    @Override
    public FieldDecl setPos(int pos) {
        return (FieldDecl) super.setPos(pos);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        FieldDecl fieldDecl = (FieldDecl) object;
        return Objects.equals(mods, fieldDecl.mods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mods);
    }
}

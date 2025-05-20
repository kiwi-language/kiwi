package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Field;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
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

    public List<Modifier> mods() {
        return mods;
    }

    @Override
    public String toString() {
        return "FieldDecl[" +
                "mods=" + mods + ", " +
                "type=" + getType() + ", " +
                "name=" + getName() + ']';
    }

}

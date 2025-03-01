package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Field;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class FieldDecl extends Decl<Field> {
    private final List<Modifier> mods;
    private final TypeNode type;
    private final Ident name;

    public FieldDecl(
            List<Modifier> mods,
            TypeNode type,
            Ident name
    ) {
        this.mods = mods;
        this.type = type;
        this.name = name;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (!mods.isEmpty()) {
            writer.write(mods, " ");
            writer.write(" ");
        }
        writer.write(type);
        writer.write(" ");
        writer.write(name);
        writer.writeln(";");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitFieldDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        mods.forEach(action);
        action.accept(name);
        action.accept(type);
    }

    public List<Modifier> mods() {
        return mods;
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
        var that = (FieldDecl) obj;
        return Objects.equals(this.mods, that.mods) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mods, type, name);
    }

    @Override
    public String toString() {
        return "FieldDecl[" +
                "mods=" + mods + ", " +
                "type=" + type + ", " +
                "name=" + name + ']';
    }

}

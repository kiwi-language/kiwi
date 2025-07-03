package org.metavm.compiler.syntax;

import org.metavm.compiler.element.LocalVar;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;

public final class LocalVarDecl extends VariableDecl<LocalVar> {

    public LocalVarDecl(@Nullable TypeNode type, Name name, @Nullable Expr initial) {
        super(List.nil(), type, name, initial);
    }

    @Override
    public void write(SyntaxWriter writer) {
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

    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitLocalVarDecl(this);
    }

    @Override
    public LocalVarDecl setPos(int pos) {
        return (LocalVarDecl) super.setPos(pos);
    }

    @Override
    public List<Modifier> getMods() {
        return List.of();
    }
}

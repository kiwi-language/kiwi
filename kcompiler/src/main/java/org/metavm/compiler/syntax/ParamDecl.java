package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.Param;

import javax.annotation.Nullable;

public final class ParamDecl extends VariableDecl<Param> {
    public ParamDecl(@Nullable TypeNode type, Name name) {
        super(type, name, null);
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(getName());
        if (getType() != null) {
            writer.write(": ");
            getType().write(writer);
        }
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitParamDecl(this);
    }

    @Override
    public String toString() {
        return "Param[" +
                "type=" + getType() + ", " +
                "name=" + getName() + ']';
    }

}

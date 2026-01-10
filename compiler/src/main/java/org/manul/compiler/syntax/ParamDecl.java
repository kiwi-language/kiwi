package org.manul.compiler.syntax;

import org.manul.compiler.element.Name;
import org.manul.compiler.element.Param;
import org.manul.compiler.util.List;

import javax.annotation.Nullable;

public final class ParamDecl extends VariableDecl<Param> {
    public ParamDecl(List<Annotation> annotations, @Nullable TypeNode type, Name name) {
        super(annotations, type, name, null, true);
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
    public ParamDecl setPos(int pos) {
        return (ParamDecl) super.setPos(pos);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitParamDecl(this);
    }

    @Override
    public List<Modifier> getMods() {
        return List.of();
    }
}

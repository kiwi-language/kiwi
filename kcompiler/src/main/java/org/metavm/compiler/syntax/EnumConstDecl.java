package org.metavm.compiler.syntax;

import org.metavm.compiler.element.EnumConst;
import org.metavm.compiler.element.MethodRef;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class EnumConstDecl extends Decl<EnumConst> {
    private final Name name;
    private List<Expr> arguments;
    private @Nullable ClassDecl decl;
    private MethodRef init;

    public EnumConstDecl(Name name, List<Expr> arguments, @Nullable ClassDecl decl) {
        this.name = name;
        this.arguments = arguments;
        this.decl = decl;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(name);
        writer.writeln(",");
    }

    @Nullable
    public ClassDecl getDecl() {
        return decl;
    }

    public void setDecl(@Nullable ClassDecl decl) {
        this.decl = decl;
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitEnumConstDecl(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        arguments.forEach(action);
        if (decl != null)
            action.accept(decl);
    }

    public Name getName() {
        return name;
    }

    public List<Expr> getArguments() {
        return arguments;
    }

    public void setArguments(List<Expr> arguments) {
        this.arguments = arguments;
    }

    public MethodRef getInit() {
        return init;
    }

    public void setInit(MethodRef init) {
        this.init = init;
    }

    @Override
    public String toString() {
        return "EnumConstantDecl[" +
                "name=" + name + ']';
    }

}

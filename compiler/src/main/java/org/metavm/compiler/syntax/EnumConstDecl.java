package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.EnumConst;
import org.metavm.compiler.element.MethodRef;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class EnumConstDecl extends Decl<EnumConst> {
    private List<Annotation> annotations;
    private final Name name;
    private List<Expr> arguments;
    private @Nullable ClassDecl decl;
    private MethodRef init;

    public EnumConstDecl(List<Annotation> annotations, Name name, List<Expr> arguments, @Nullable ClassDecl decl) {
        this.annotations = annotations;
        this.name = name;
        this.arguments = arguments;
        this.decl = decl;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write(name);
        if (!arguments.isEmpty()) {
            writer.write("(");
            writer.write(arguments);
            writer.write(")");
        }
        if (decl != null) {
            writer.write("(");
            writer.write(decl.getImplements().head().getArgs());
            writer.write(")");
            decl.writeBody(writer);
        }
        writer.writeln(",");
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    @Nullable
    public ClassDecl getDecl() {
        return decl;
    }

    public Clazz getActualClass() {
        return decl != null ? decl.getElement() : getElement().getDeclClass();
    }

    public void setDecl(@Nullable ClassDecl decl) {
        this.decl = decl;
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitEnumConstDecl(this);
    }

    @Override
    public EnumConstDecl setPos(int pos) {
        return (EnumConstDecl) super.setPos(pos);
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        EnumConstDecl that = (EnumConstDecl) object;
        return Objects.equals(annotations, that.annotations) && Objects.equals(name, that.name) && Objects.equals(arguments, that.arguments) && Objects.equals(decl, that.decl) && Objects.equals(init, that.init);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotations, name, arguments, decl, init);
    }
}

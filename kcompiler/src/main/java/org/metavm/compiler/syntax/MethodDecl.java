package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Method;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public final class MethodDecl extends Decl<Method> {
    private final List<Modifier> mods;
    private List<Annotation> annotations;
    private final List<TypeVariableDecl> typeParameters;
    private final Name name;
    private List<ParamDecl> params;
    @Nullable
    private final TypeNode returnType;
    @Nullable
    private final Block body;

    public MethodDecl(
            List<Modifier> mods,
            List<Annotation> annotations,
            List<TypeVariableDecl> typeParameters,
            Name name,
            List<ParamDecl> params,
            @Nullable TypeNode returnType,
            @Nullable Block body
    ) {
        this.mods = mods;
        this.annotations = annotations;
        this.typeParameters = typeParameters;
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
    }

    @Override
    public void write(SyntaxWriter writer) {
        if (!mods.isEmpty()) {
            writer.write(mods, " ");
            writer.write(" ");
        }
        if (name != Name.init())
            writer.write("func ");
        writer.write(name);
        if (typeParameters.nonEmpty()) {
            writer.write("<");
            writer.write(typeParameters);
            writer.write(">");
        }
        writer.write("(");
        writer.write(params, ", ");
        writer.write(")");
        if (returnType != null) {
            writer.write(" -> ");
            writer.write(returnType);
        }
        if (body != null) {
            writer.write(" ");
            writer.write(body);
        }
        writer.writeln();
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitMethodDecl(this);
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        mods.forEach(action);
        typeParameters.forEach(action);
        params.forEach(action);
        if (returnType != null) action.accept(returnType);
        if (body != null) action.accept(body);
    }

    public List<Modifier> mods() {
        return mods;
    }

    public Name name() {
        return name;
    }

    public List<TypeVariableDecl> getTypeParameters() {
        return typeParameters;
    }

    public List<ParamDecl> getParams() {
        return params;
    }

    public void setParams(List<ParamDecl> params) {
        this.params = params;
    }

    @Nullable
    public TypeNode returnType() {
        return returnType;
    }

    @Nullable
    public Block body() {
        return body;
    }

    public boolean isInit() {
        return name == Name.init();
    }

    public boolean isPrimaryInit() {
        if (isInit()) {
            var stmts = Objects.requireNonNull(this.body).getStmts();
            return stmts.isEmpty() || !Nodes.isSelfInitCall(stmts.getFirst());
        }
        else
            return false;
    }

    @Override
    public MethodDecl setPos(int pos) {
        return (MethodDecl) super.setPos(pos);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MethodDecl that = (MethodDecl) object;
        return Objects.equals(mods, that.mods) && Objects.equals(annotations, that.annotations) && Objects.equals(typeParameters, that.typeParameters) && Objects.equals(name, that.name) && Objects.equals(params, that.params) && Objects.equals(returnType, that.returnType) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mods, annotations, typeParameters, name, params, returnType, body);
    }

}

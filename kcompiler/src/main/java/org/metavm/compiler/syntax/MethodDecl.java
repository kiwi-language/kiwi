package org.metavm.compiler.syntax;

import org.metavm.compiler.element.Method;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class MethodDecl extends Decl<Method> {
    private final List<Modifier> mods;
    private final List<TypeVariableDecl> typeParameters;
    private final Ident name;
    private List<ParamDecl> params;
    @Nullable
    private final TypeNode returnType;
    @Nullable
    private final Block body;

    public MethodDecl(
            List<Modifier> mods,
            List<TypeVariableDecl> typeParameters,
            Ident name,
            List<ParamDecl> params,
            @Nullable TypeNode returnType,
            @Nullable Block body
    ) {
        this.mods = mods;
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

    @Override
    public void forEachChild(Consumer<Node> action) {
        mods.forEach(action);
        typeParameters.forEach(action);
        action.accept(name);
        params.forEach(action);
        if (returnType != null) action.accept(returnType);
        if (body != null) action.accept(body);
    }

    public List<Modifier> mods() {
        return mods;
    }

    public Ident name() {
        return name;
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


}

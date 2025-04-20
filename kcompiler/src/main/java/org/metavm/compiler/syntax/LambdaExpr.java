package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Lambda;
import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Slf4j
public final class LambdaExpr extends Expr {
    private final List<ParamDecl> params;
    @Nullable
    private TypeNode returnType;
    private final Node body;
    private Lambda element;

    public LambdaExpr(
            List<ParamDecl> params,
            @Nullable TypeNode returnType,
            Node body
    ) {
        this.params = params;
        this.returnType = returnType;
        this.body = body;
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("(");
        writer.write(params);
        writer.write(")");
        if (returnType != null) {
            writer.write(": ");
            writer.write(returnType);
        }
        writer.write(" -> ");
        writer.write(body);
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitLambdaExpr(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {
        params.forEach(action);
        if (returnType != null)
            action.accept(returnType);
        action.accept(body);
    }

    public List<ParamDecl> params() {
        return params;
    }

    @Nullable
    public TypeNode returnType() {
        return returnType;
    }

    public Node body() {
        return body;
    }

    @Override
    public String toString() {
        return "LambdaExpr[" +
                "params=" + params + ", " +
                "returnType=" + returnType + ", " +
                "body=" + body + ']';
    }

    public Lambda getElement() {
        return element;
    }

    public void setElement(Lambda lambda) {
        this.element = lambda;
    }

    @Nullable
    public TypeNode getReturnType() {
        return returnType;
    }

    public void setTargetType(FuncType targetType) {
        var lambda = getElement();
        if (returnType == null) {
            returnType = targetType.makeNode();
            lambda.setRetType(targetType.getRetType());
        }
        var paramTypeIt = targetType.getParamTypes().iterator();
        for (ParamDecl paramDecl : params) {
            var paramType = paramTypeIt.next();
            if (paramDecl.getType() == null) {
                paramDecl.setType(paramType.makeNode());
                paramDecl.getElement().setType(paramType);
            }
        }
    }
}

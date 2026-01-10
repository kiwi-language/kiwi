package org.manul.compiler.syntax;

import org.manul.compiler.analyze.Env;
import org.manul.compiler.type.ErrorType;
import org.manul.compiler.type.Type;

import java.util.function.Consumer;

public class ErrorTypeNode extends TypeNode {

    public ErrorTypeNode() {
    }

    @Override
    public void write(SyntaxWriter writer) {
        writer.write("<error>");
    }

    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visitErrorType(this);
    }

    @Override
    public void forEachChild(Consumer<Node> action) {

    }

    @Override
    protected Type actualResolve(Env env) {
        return ErrorType.instance;
    }
}

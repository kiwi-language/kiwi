package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

@EntityType
public class FunctionNode extends Node {

    private final FunctionType functionType;

    public FunctionNode(String name, Node previous, Code code, FunctionType functionType) {
        super(name, null, previous, code);
        this.functionType = functionType;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("call " + functionType.toExpression());
    }

    @Override
    public int getStackChange() {
        var paramCount = functionType.getParameterTypes().size();
        return functionType.isVoid() ? -paramCount - 1 : paramCount;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.FUNC);
        output.writeConstant(functionType);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionNode(this);
    }

    @Nullable
    @Override
    public Type getType() {
        var type = functionType.getReturnType();
        return type.isVoid() ? null : type;
    }
}

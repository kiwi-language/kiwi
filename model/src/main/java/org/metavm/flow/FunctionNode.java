package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Entity
public class FunctionNode extends Node {

    private final FunctionType functionType;

    public FunctionNode(String name, Node previous, Code code, FunctionType functionType) {
        super(name, null, previous, code);
        this.functionType = functionType;
    }

    public static Node read(CodeInput input, String name) {
        return new FunctionNode(name, input.getPrev(), input.getCode(), (FunctionType) input.readConstant());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("call " + functionType.getText());
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

    @Nullable
    @Override
    public Type getType() {
        var type = functionType.getReturnType();
        return type.isVoid() ? null : type;
    }

    @Override
    public boolean hasOutput() {
        return !functionType.isVoid();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        functionType.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        functionType.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}

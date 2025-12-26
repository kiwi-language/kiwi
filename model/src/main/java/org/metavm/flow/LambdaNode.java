package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class LambdaNode extends Node {

    private final LambdaRef lambda;
    private final @Nullable ClassType functionalInterface;

    public LambdaNode(String name, Node previous, Code code,
                      @NotNull LambdaRef lambda, @Nullable ClassType functionalInterface) {
        super(name, functionalInterface != null ? functionalInterface : lambda.getFunctionType(), previous, code);
        this.functionalInterface = functionalInterface;
        this.lambda = lambda;
    }

    public static Node read(CodeInput input, String name) {
        return new LambdaNode(name, input.getPrev(), input.getCode(), (LambdaRef) input.readConstant(),
                (ClassType) input.readNullable(input::readConstant));
    }

    @Override
    @NotNull
    public Type getType() {
        return Objects.requireNonNull(super.getType());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Nullable
    public ClassType getFunctionalInterface() {
        return functionalInterface;
    }

    @Override
    public void writeContent(CodeWriter writer) {
//        writer.write("lambda: (" + Utils.join(getLambda().getParameters(), Parameter::getText, ", ") + ")");
//        writer.write(" -> " + getLambda().getReturnType().getName());
        writer.writeln();
        writer.indent();
        getLambda().writeCode(writer);
        writer.unindent();
    }

    public Lambda getLambda() {
        return lambda.getRawLambda();
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LAMBDA);
        output.writeConstant(lambda);
        output.writeNullable(functionalInterface, output::writeConstant);
    }

    @Override
    public int getLength() {
        return functionalInterface == null ? 4 : 6;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLambdaNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        lambda.accept(visitor);
        if (functionalInterface != null) functionalInterface.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        lambda.forEachReference(action);
        if (functionalInterface != null) functionalInterface.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}

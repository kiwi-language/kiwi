package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class LambdaNode extends Node {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final LambdaRef lambda;
    private final @Nullable ClassType functionalInterface;

    public LambdaNode(String name, Node previous, Code code,
                      @NotNull LambdaRef lambda, @Nullable ClassType functionalInterface) {
        super(name, functionalInterface != null ? functionalInterface : lambda.getFunctionType(), previous, code);
        this.functionalInterface = functionalInterface;
        this.lambda = lambda;
    }

    public static Node read(CodeInput input, String name) {
        return new LambdaNode(name, input.getPrev(), input.getCode(), (LambdaRef) input.readConstant(), (ClassType) input.readConstant());
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
        writer.write("(" + Utils.join(getLambda().getParameters(), Parameter::getText, ", ") + ")");
        writer.write(": " + getLambda().getReturnType().getName());
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
        if(functionalInterface != null) {
            output.writeBoolean(true);
            output.writeConstant(functionalInterface);
        }
        else
            output.writeBoolean(false);
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
    public void buildJson(Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        var functionalInterface = this.getFunctionalInterface();
        if (functionalInterface != null) map.put("functionalInterface", functionalInterface.toJson());
        map.put("lambda", this.getLambda().getStringId());
        map.put("stackChange", this.getStackChange());
        map.put("length", this.getLength());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        var successor = this.getSuccessor();
        if (successor != null) map.put("successor", successor.getStringId());
        var predecessor = this.getPredecessor();
        if (predecessor != null) map.put("predecessor", predecessor.getStringId());
        map.put("code", this.getCode().toJson());
        map.put("exit", this.isExit());
        map.put("unconditionalJump", this.isUnconditionalJump());
        map.put("sequential", this.isSequential());
        var error = this.getError();
        if (error != null) map.put("error", error);
        map.put("expressionTypes", this.getExpressionTypes());
        map.put("nextExpressionTypes", this.getNextExpressionTypes());
        map.put("offset", this.getOffset());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}

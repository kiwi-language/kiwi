package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public class InstanceOfNode extends Node {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final Type targetType;

    public InstanceOfNode(String name,
                          @Nullable Node previous,
                          @NotNull Code code,
                          Type targetType) {
        super(name, Types.getBooleanType(), previous, code);
       this.targetType = targetType;
    }

    public static Node read(CodeInput input, String name) {
        return new InstanceOfNode(name, input.getPrev(), input.getCode(), (Type) input.readConstant());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("instanceof");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INSTANCE_OF);
        output.writeConstant(targetType);
    }

    @Override
    public int getLength() {
        return 3;
    }

    public Type getTargetType() {
        return targetType;
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getBooleanType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitInstanceOfNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        targetType.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        targetType.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("stackChange", this.getStackChange());
        map.put("length", this.getLength());
        map.put("targetType", this.getTargetType().toJson());
        map.put("type", this.getType().toJson());
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

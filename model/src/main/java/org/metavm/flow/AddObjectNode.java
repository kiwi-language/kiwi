package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class AddObjectNode extends Node {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private boolean ephemeral;

    public AddObjectNode(String name, boolean ephemeral, ClassType type, Node prev,
                         Code code) {
        super(name, type, prev, code);
        this.ephemeral = ephemeral;
    }

    public static Node read(CodeInput input, String name) {
        return new AddObjectNode(name, input.readBoolean(), (ClassType) input.readConstant(), input.getPrev(), input.getCode());
    }

    @Override
    @NotNull
    public ClassType getType() {
        return (ClassType) Objects.requireNonNull(super.getType());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    protected void setOutputType(@Nullable Type outputType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("allocate " + getType().getTypeDesc());
    }

    @Override
    public int getStackChange() {
        return 1 - getType().getKlass().getAllFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.ADD_OBJECT);
        output.writeConstant(getType());
        output.writeBoolean(ephemeral);
    }

    @Override
    public int getLength() {
        return 4;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    @Override
    @NotNull
    public Node getSuccessor() {
        return Objects.requireNonNull(super.getSuccessor());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddObjectNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("stackChange", this.getStackChange());
        map.put("length", this.getLength());
        map.put("successor", this.getSuccessor().getStringId());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
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

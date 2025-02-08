package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class NewObjectNode extends Node {

    public static final Logger logger = LoggerFactory.getLogger(NewObjectNode.class);
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private boolean ephemeral;

    private boolean unbound;

    public NewObjectNode(String name, ClassType type,
                         Node prev, Code code,
                         boolean ephemeral, boolean unbound) {
        super(name, type, prev, code);
        this.ephemeral = ephemeral;
        this.unbound = unbound;
    }

    public static Node read(CodeInput input, String name) {
        return new NewObjectNode(name, (ClassType) input.readConstant(), input.getPrev(), input.getCode(), input.readBoolean(), input.readBoolean());
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
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getTypeDesc());
        if (ephemeral)
            writer.write(" ephemeral");
        if (unbound)
            writer.write(" unbound");
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW);
        output.writeConstant(getType());
        output.writeBoolean(ephemeral);
        output.writeBoolean(unbound);
    }

//    public static NewObjectNode read(CodeInput input) {
//        return new NewObjectNode(
//                "NEW",
//                (ClassType) input.readConstant(),
//                input.getPrev(),
//                input.getCode(),
//                input.readBoolean(),
//                input.readBoolean()
//        );
//    }

    @Override
    public int getLength() {
        return 5;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public void setUnbound(boolean unbound) {
        this.unbound = unbound;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewObjectNode(this);
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

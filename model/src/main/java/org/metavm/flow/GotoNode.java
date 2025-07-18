package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class GotoNode extends BranchNode {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public GotoNode(@NotNull String name, @Nullable Node previous, @NotNull Code code,
                    @Nullable LabelNode target) {
        super(name, null, previous, code);
        if (target != null)
            setTarget(target);
    }

    public GotoNode(@NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(name, null, previous, code);
    }

    public static Node read(CodeInput input, String name) {
        return new GotoNode(name, input.getPrev(), input.getCode(), input.readLabel());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("goto " + getTarget().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GOTO);
        output.writeShort(getTarget().getOffset() - getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

    public void setTarget(@NotNull LabelNode target) {
        super.setTarget(target);
    }

    @Override
    public boolean isUnconditionalJump() {
        return true;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGotoNode(this);
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
        map.put("stackChange", this.getStackChange());
        map.put("length", this.getLength());
        map.put("unconditionalJump", this.isUnconditionalJump());
        map.put("target", this.getTarget().getStringId());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        var successor = this.getSuccessor();
        if (successor != null) map.put("successor", successor.getStringId());
        var predecessor = this.getPredecessor();
        if (predecessor != null) map.put("predecessor", predecessor.getStringId());
        map.put("code", this.getCode().toJson());
        map.put("exit", this.isExit());
        map.put("sequential", this.isSequential());
        var error = this.getError();
        if (error != null) map.put("error", error);
        var type = this.getType();
        if (type != null) map.put("type", type.toJson());
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

package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Entity
@Slf4j
public class GotoNode extends BranchNode {

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
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}

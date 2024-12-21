package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;

import javax.annotation.Nullable;

public class GotoNode extends JumpNode {

    public GotoNode(@NotNull String name, @Nullable Node previous, @NotNull Code code,
                    @Nullable LabelNode target) {
        super(name, null, previous, code);
        if (target != null)
            setTarget(target);
    }

    public GotoNode(@NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGotoNode(this);
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
}

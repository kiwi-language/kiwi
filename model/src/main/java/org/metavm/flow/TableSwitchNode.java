package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ReadWriteArray;

import javax.annotation.Nullable;
import java.util.List;

@Slf4j
public class TableSwitchNode extends Node {

    private Node defaultTarget = this;
    @ChildEntity
    private final ReadWriteArray<Node> targets = addChild(new ReadWriteArray<>(Node.class), "targets");
    private final int low;
    private final int high;

    public TableSwitchNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int low, int high) {
        super(name, null, previous, code);
        this.low = low;
        this.high = high;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTableSwitchNode(this);
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("tableswitch");
        writer.indent();
        writer.writeNewLine("default: " + defaultTarget.getName());
        int i = 0;
        for (Node target : targets) {
            writer.writeNewLine(i++ + ": " + target.getName());
        }
        writer.unindent();
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TABLESWITCH);
        var paddings = calcPaddings();
        for(int i = 0; i < paddings; i++)
            output.write(0);
        var base = getOffset();
        output.writeFixedInt(defaultTarget.getOffset() - base);
        output.writeFixedInt(low);
        output.writeFixedInt(high);
        targets.forEach(t -> output.writeFixedInt(t.getOffset() - base));
    }

    private int calcPaddings() {
        var offset = getOffset();
        return (offset & ~3) + 3 - offset;
    }

    @Override
    public int getLength() {
        return (targets.size() << 2) + 13 + calcPaddings();
    }

    public void setDefaultTarget(Node defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public void addTarget(Node target) {
        targets.add(target);
    }

    public void setTargets(List<Node> targets) {
        this.targets.reset(targets);
    }

    public Node getDefaultTarget() {
        return defaultTarget;
    }
}

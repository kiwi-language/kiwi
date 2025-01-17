package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class TableSwitchNode extends SwitchNode {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final int low;
    private final int high;

    public TableSwitchNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, int low, int high) {
        super(name, null, previous, code);
        this.low = low;
        this.high = high;
    }

    public static Node read(CodeInput input, String name) {
        input.skipPadding();
        var defaultTarget = input.readLabel();
        var low = input.readFixedInt();
        var high = input.readFixedInt();
        var targets = new ArrayList<LabelNode>();
        for (int i = low; i <= high; i++)
            targets.add(input.readLabel());
        var node = new TableSwitchNode(name, input.getPrev(), input.getCode(), low, high);
        node.setDefaultTarget(defaultTarget);
        node.setTargets(targets);
        return node;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.writeln("tableswitch");
        writer.indent();
        writer.writeln("low: " + low);
        writer.writeln("high: " + high);
        writer.writeln("default: " + defaultTarget.getName());
        int i = 0;
        for (Node target : targets) {
            writer.writeln(i++ + ": " + target.getName());
        }
        writer.unindent();
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TABLE_SWITCH);
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTableSwitchNode(this);
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
        map.put("defaultTarget", this.getDefaultTarget().getStringId());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        var successor = this.getSuccessor();
        if (successor != null) map.put("successor", successor.getStringId());
        var predecessor = this.getPredecessor();
        if (predecessor != null) map.put("predecessor", predecessor.getStringId());
        map.put("code", this.getCode().getStringId());
        map.put("exit", this.isExit());
        map.put("unconditionalJump", this.isUnconditionalJump());
        map.put("sequential", this.isSequential());
        var error = this.getError();
        if (error != null) map.put("error", error);
        var type = this.getType();
        if (type != null) map.put("type", type.toJson());
        map.put("expressionTypes", this.getExpressionTypes());
        map.put("text", this.getText());
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

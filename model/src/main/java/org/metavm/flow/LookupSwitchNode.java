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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class LookupSwitchNode extends SwitchNode {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final List<Integer> matches = new ArrayList<>();

    public LookupSwitchNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, List<Integer> matches) {
        super(name, null, previous, code);
        this.matches.addAll(matches);
    }

    public static Node read(CodeInput input, String name) {
        input.skipPadding();
        var defaultTarget = input.readLabel();
        var numMatches = input.readFixedInt();
        var matches = new ArrayList<Integer>();
        var targets = new ArrayList<LabelNode>();
        for (int i = 0; i < numMatches; i++) {
            matches.add(input.readFixedInt());
            targets.add(input.readLabel());
        }
        var node = new LookupSwitchNode(name, input.getPrev(), input.getCode(), matches);
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
        writer.writeln("lookupswitch");
        writer.indent();
        writer.writeln("default: " + defaultTarget.getName());
        var matchIt = matches.iterator();
        for (Node target : targets) {
            writer.writeln(matchIt.next() + ": " + target.getName());
        }
        writer.unindent();
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LOOKUP_SWITCH);
        var paddings = calcPaddings();
        for(int i = 0; i < paddings; i++)
            output.write(0);
        var base = getOffset();
        output.writeFixedInt(defaultTarget.getOffset() - base);
        output.writeFixedInt(matches.size());
        var matchIt = matches.iterator();
        targets.forEach(t -> {
            output.writeFixedInt(matchIt.next());
            output.writeFixedInt(t.getOffset() - base);
        });
    }

    private int calcPaddings() {
        var offset = getOffset();
        return (offset & ~3) + 3 - offset;
    }

    @Override
    public int getLength() {
        return (targets.size() << 3) + 9 + calcPaddings();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLookupSwitchNode(this);
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

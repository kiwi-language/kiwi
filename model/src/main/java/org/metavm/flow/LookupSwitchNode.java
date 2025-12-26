package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Entity
@Slf4j
public class LookupSwitchNode extends SwitchNode {

    private final List<Integer> matches = new ArrayList<>();

    public LookupSwitchNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, List<Integer> matches) {
        super(name, null, previous, code);
        this.matches.addAll(matches);
    }

    public static Node read(CodeInput input, String name) {
        var base = input.getOffset();
        input.skipPadding();
        var defaultTarget = input.getLabel(base + input.readFixedInt());
        var numMatches = input.readFixedInt();
        var matches = new ArrayList<Integer>();
        var targets = new ArrayList<LabelNode>();
        for (int i = 0; i < numMatches; i++) {
            matches.add(input.readFixedInt());
            targets.add(input.getLabel(base + input.readFixedInt()));
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
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}

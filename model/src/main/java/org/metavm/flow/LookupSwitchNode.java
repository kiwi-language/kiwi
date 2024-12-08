package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ReadWriteArray;

import javax.annotation.Nullable;
import java.util.List;

@Slf4j
public class LookupSwitchNode extends SwitchNode {

    @ChildEntity
    private final ReadWriteArray<Integer> matches = addChild(new ReadWriteArray<>(Integer.class), "matches");

    public LookupSwitchNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, List<Integer> matches) {
        super(name, null, previous, code);
        this.matches.reset(matches);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLookupSwitch(this);
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("lookupswitch");
        writer.indent();
        writer.writeNewLine("default: " + defaultTarget.getName());
        var matchIt = matches.iterator();
        for (Node target : targets) {
            writer.writeNewLine(matchIt.next() + ": " + target.getName());
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

}

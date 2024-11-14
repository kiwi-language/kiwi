package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ReadWriteArray;
import org.metavm.flow.rest.Bytecodes;

import javax.annotation.Nullable;
import java.util.List;

public class TargetNode extends Node {

    @ChildEntity
    private final ReadWriteArray<GotoNode> sources = addChild(new ReadWriteArray<>(GotoNode.class), "sources");

    public TargetNode(Long tmpId, @NotNull String name, @Nullable Node previous, @NotNull Code code) {
        super(tmpId, name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTargetNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("LabelNode");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TARGET);
    }

    @Override
    public int getLength() {
        return 1;
    }

    public void setSources(List<GotoNode> sources) {
        this.sources.reset(sources);
    }

    public void addSource(GotoNode source) {
        this.sources.add(source);
    }

}

package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

@EntityType
public class IndexCountNode extends Node {

    private final IndexRef indexRef;

    public IndexCountNode(String name, Node previous, Code code, IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexCount(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (getIndex().getFields().size() << 1);
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_COUNT);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexCountNode(this);
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getLongType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }
}

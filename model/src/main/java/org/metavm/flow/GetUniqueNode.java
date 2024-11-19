package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.UnionType;

@EntityType
public class GetUniqueNode extends Node {

    private final IndexRef indexRef;

    public GetUniqueNode(String name, UnionType type, IndexRef indexRef, Node previous, Code code) {
        super(name, type, previous, code);
        this.indexRef = indexRef;
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getUnique(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - getIndex().getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_UNIQUE);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetUniqueNode(this);
    }
}

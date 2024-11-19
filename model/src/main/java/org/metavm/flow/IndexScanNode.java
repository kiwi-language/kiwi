package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;

@EntityType
public class IndexScanNode extends Node {

    private final IndexRef indexRef;

    public IndexScanNode(String name, Node previous, Code code, IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    public ArrayType getType() {
        return new ArrayType(getIndex().getDeclaringType().getType(), ArrayKind.READ_ONLY);
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexScan(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (getIndex().getFields().size() << 1);
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SCAN);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexQueryNode(this);
    }
}

package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.IndexRef;

@Entity
public class IndexScanNode extends Node {

    private final IndexRef indexRef;

    public IndexScanNode(String name, Node previous, Code code, IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    public ArrayType getType() {
        return new ArrayType(indexRef.getDeclaringType(), ArrayKind.READ_ONLY);
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexScan(" + indexRef.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (indexRef.getFieldCount() << 1);
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

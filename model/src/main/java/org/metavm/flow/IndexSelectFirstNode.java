
package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

@EntityType
public class IndexSelectFirstNode extends Node {

    private final IndexRef indexRef;

    public IndexSelectFirstNode(String name, Node previous, Code code,
                                IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public Type getType() {
        return Types.getNullableType(indexRef.resolve().getDeclaringType().getType());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelectFirst(" + indexRef.resolve().getName() + ", " +  ")");
    }

    @Override
    public int getStackChange() {
        return 1 - indexRef.resolve().getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SELECT_FIRST);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexSelectFirstNode(this);
    }

}

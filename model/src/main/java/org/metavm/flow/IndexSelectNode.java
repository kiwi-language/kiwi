package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;

import java.util.List;

@EntityType
public class IndexSelectNode extends Node {

    private final IndexRef indexRef;

    public IndexSelectNode(String name, Node previous, Code code,
                           IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    @NotNull
    public ClassType getType() {
        return new ClassType(StdKlass.arrayList.get(), List.of(getIndex().getDeclaringType().getType()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelect(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - getIndex().getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SELECT);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexSelectNode(this);
    }
}

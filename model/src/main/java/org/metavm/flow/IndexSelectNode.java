package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.IndexRef;

import java.util.List;

@Entity
public class IndexSelectNode extends Node {

    private final IndexRef indexRef;

    public IndexSelectNode(String name, Node previous, Code code,
                           IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    @NotNull
    public ClassType getType() {
        return new ClassType(null, StdKlass.arrayList.get(), List.of(indexRef.getDeclaringType()));
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelect(" + indexRef.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - indexRef.getFieldCount();
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

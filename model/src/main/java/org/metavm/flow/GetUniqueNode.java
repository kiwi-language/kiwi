package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.UnionType;

import java.util.function.Consumer;

@Entity
public class GetUniqueNode extends Node {

    private final IndexRef indexRef;

    public GetUniqueNode(String name, UnionType type, IndexRef indexRef, Node previous, Code code) {
        super(name, type, previous, code);
        this.indexRef = indexRef;
    }

    public static Node read(CodeInput input, String name) {
        return new GetUniqueNode(name, (UnionType) input.readConstant(), (IndexRef) input.readConstant(), input.getPrev(), input.getCode());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getUnique(" + indexRef.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 0;
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        indexRef.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        indexRef.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}

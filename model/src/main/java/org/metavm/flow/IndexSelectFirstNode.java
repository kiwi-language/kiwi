
package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import java.util.function.Consumer;

@Entity
public class IndexSelectFirstNode extends Node {

    private final IndexRef indexRef;

    public IndexSelectFirstNode(String name, Node previous, Code code,
                                IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    public static Node read(CodeInput input, String name) {
        return new IndexSelectFirstNode(name, input.getPrev(), input.getCode(), (IndexRef) input.readConstant());
    }

    @Override
    public Type getType() {
        return Types.getNullableType(indexRef.getDeclaringType());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelectFirst(" + indexRef.getName() + ", " +  ")");
    }

    @Override
    public int getStackChange() {
        return 0;
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

package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Entity;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.IndexRef;
import org.manul.object.type.Type;
import org.manul.object.type.Types;

import java.util.function.Consumer;

@Entity
public class IndexCountNode extends Node {

    private final IndexRef indexRef;

    public IndexCountNode(String name, Node previous, Code code, IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    public static Node read(CodeInput input, String name) {
        return new IndexCountNode(name, input.getPrev(), input.getCode(), (IndexRef) input.readConstant());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexCount(" + indexRef.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return -1;
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

    @NotNull
    @Override
    public Type getType() {
        return Types.getLongType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexCountNode(this);
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

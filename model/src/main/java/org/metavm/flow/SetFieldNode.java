package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.FieldRef;

import java.util.function.Consumer;

@Entity
public class SetFieldNode extends Node {

    private final FieldRef fieldRef;

    public SetFieldNode(String name, Node prev, Code code, FieldRef fieldRef) {
        super(name, null, prev, code);
        this.fieldRef = fieldRef;
    }

    public static Node read(CodeInput input, String name) {
        return new SetFieldNode(name, input.getPrev(), input.getCode(), (FieldRef) input.readConstant());
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setfield " + fieldRef.getRawField().getName());
    }

    @Override
    public int getStackChange() {
        return -2;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.SET_FIELD);
        output.writeConstant(fieldRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetFieldNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        fieldRef.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        fieldRef.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}

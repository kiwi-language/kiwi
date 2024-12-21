package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.FieldRef;

@Entity
public class SetChildFieldNode extends Node {

    private final FieldRef fieldRef;

    public SetChildFieldNode(String name, Node prev, Code code, FieldRef fieldRef) {
        super(name, null, prev, code);
        this.fieldRef = fieldRef;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setchildfield " + fieldRef.getRawField().getName());
    }

    @Override
    public int getStackChange() {
        return -2;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.SET_CHILD_FIELD);
        output.writeConstant(fieldRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetChildField(this);
    }
}

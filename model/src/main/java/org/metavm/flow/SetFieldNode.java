package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.FieldRef;

@EntityType
public class SetFieldNode extends Node {

    private final FieldRef fieldRef;

    public SetFieldNode(String name, Node prev, Code code, FieldRef fieldRef) {
        super(name, null, prev, code);
        this.fieldRef = fieldRef;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setField " + fieldRef.getRawField().getName());
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
}

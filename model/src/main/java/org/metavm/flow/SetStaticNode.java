package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.FieldRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityType
public class SetStaticNode extends Node {

    public static final Logger logger = LoggerFactory.getLogger(SetStaticNode.class);

    private final FieldRef fieldRef;

    public SetStaticNode(String name, Node previous, Code code, FieldRef fieldRef) {
        super(name, null, previous, code);
        this.fieldRef = fieldRef;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setstatic " + fieldRef);
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.SET_STATIC);
        output.writeConstant(fieldRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetStaticNode(this);
    }
}

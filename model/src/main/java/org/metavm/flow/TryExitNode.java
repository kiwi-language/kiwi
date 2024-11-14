package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;

@EntityType
public class TryExitNode extends Node {

    private final int variableIndex;

    public TryExitNode(Long tmpId, String name, Node previous, Code code, int variableIndex) {
        super(tmpId, name, null, previous, code);
        this.variableIndex = variableIndex;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("try-exit variable = " + variableIndex);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TRY_EXIT);
        output.writeShort(variableIndex);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryExitNode(this);
    }

    public int getVariableIndex() {
        return variableIndex;
    }
}

package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.util.BusinessException;

@Entity
public class ClearArrayNode extends Node {

    public ClearArrayNode(String name,
                          Node previous, Code code) {
        super(name, null, previous, code);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClearArrayNode(this);
    }

    private Value check(@NotNull Value array) {
        if(!array.getType().isArray())
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        return array;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("arrayclear");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.CLEAR_ARRAY);
    }

    @Override
    public int getLength() {
        return 1;
    }

}

package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;

@EntityType
@Slf4j
public class ReturnNode extends Node {

    public ReturnNode(Long tmpId, String name, Node prev, Code code) {
        super(tmpId, name, null, prev, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("return");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.RETURN);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    @NotNull
    public Type getType() {
        return getCode().getCallable().getReturnType();
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitReturnNode(this);
    }

}

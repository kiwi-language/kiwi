package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetFieldNode extends Node {

    private final FieldRef fieldRef;

    public GetFieldNode(String name,
                        @Nullable Node previous,
                        @NotNull Code code,
                        FieldRef fieldRef) {
        super(name, null, previous, code);
        this.fieldRef = fieldRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetFieldNode(this);
    }

    @NotNull
    public Type getType() {
        return fieldRef.getPropertyType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getfield " + fieldRef);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_FIELD);
        output.writeConstant(fieldRef);
    }

    @Override
    public int getLength() {
        return 3;
    }
}

package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetPropertyNode extends Node {

    private final PropertyRef propertyRef;

    public GetPropertyNode(String name,
                           @Nullable Node previous,
                           @NotNull Code code,
                           PropertyRef propertyRef) {
        super(name, null, previous, code);
        this.propertyRef = propertyRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetFieldNode(this);
    }

    @NotNull
    public Type getType() {
        return propertyRef.resolve().getType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getProperty " + propertyRef);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_PROPERTY);
        output.writeConstant(propertyRef);
    }

    @Override
    public int getLength() {
        return 3;
    }
}

package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetPropertyNode extends Node {

    private final PropertyRef propertyRef;

    public GetPropertyNode(Long tmpId,
                           @NotNull String name,
                           @Nullable Node previous,
                           @NotNull Code code,
                           PropertyRef propertyRef) {
        super(tmpId, name, null, previous, code);
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
    public void writeContent(CodeWriter writer) {
        writer.write("getProperty " + propertyRef.resolve().getName());
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

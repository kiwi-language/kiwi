package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetStaticNode extends Node {

    private final PropertyRef propertyRef;

    public GetStaticNode(String name,
                         @Nullable Node previous,
                         @NotNull Code code,
                         PropertyRef propertyRef) {
        super(name, null, previous, code);
        this.propertyRef = propertyRef;
    }

    @NotNull
    @Override
    public Type getType() {
        return propertyRef.resolve().getType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetStaticNode(this);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getstatic " + propertyRef);
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_STATIC);
        output.writeConstant(propertyRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

}

package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;
import org.metavm.object.view.ObjectMappingRef;

@EntityType
public class UnmapNode extends Node {

    private final ObjectMappingRef mappingRef;

    public UnmapNode(Long tmpId, @NotNull String name, @Nullable Node previous, @NotNull Code code,
                     ObjectMappingRef mappingRef) {
        super(tmpId, name, null, previous, code);
        this.mappingRef = mappingRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnmapNode(this);
    }

    @NotNull
    @Override
    public Type getType() {
        return mappingRef.resolve().getSourceType();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("unmap " + mappingRef.resolve().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.UNMAP);
        output.writeConstant(mappingRef);
    }

    @Override
    public int getLength() {
        return 3;
    }
}

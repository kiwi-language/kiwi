package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.object.type.Type;
import org.metavm.object.view.ObjectMappingRef;

@EntityType
public class MapNode extends Node {

    private final ObjectMappingRef mappingRef;

    public MapNode(@NotNull String name, @Nullable Node previous, @NotNull Code code,
                   ObjectMappingRef mappingRef) {
        super(name, null, previous, code);
        this.mappingRef = mappingRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMapNode(this);
    }

    @NotNull
    @Override
    public Type getType() {
        return mappingRef.resolve().getTargetType();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("map " + mappingRef.resolve().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.MAP);
        output.writeConstant(mappingRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

}

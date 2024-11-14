package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.MapNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.view.ObjectMappingRef;

@EntityType
public class MapNode extends NodeRT {

    public static MapNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (MapNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (MapNodeParam) nodeDTO.param();
            var mappingRef = ObjectMappingRef.create(param.mappingRef(), context);
            node = new MapNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, mappingRef);
        }
        return node;
    }

    private final ObjectMappingRef mappingRef;

    public MapNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull Code code,
                   ObjectMappingRef mappingRef) {
        super(tmpId, name, null, previous, code);
        this.mappingRef = mappingRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMapNode(this);
    }

    @Override
    protected MapNodeParam getParam(SerializeContext serializeContext) {
        return new MapNodeParam(mappingRef.toDTO(serializeContext));
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

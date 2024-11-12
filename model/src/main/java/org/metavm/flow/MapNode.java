package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.MapNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.object.view.ObjectMappingRef;

@EntityType
public class MapNode extends NodeRT {

    public static MapNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (MapNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (MapNodeParam) nodeDTO.param();
            var mappingRef = ObjectMappingRef.create(param.mappingRef(), context);
            node = new MapNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, mappingRef);
        }
        return node;
    }

    private final ObjectMappingRef mappingRef;

    public MapNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                   ObjectMappingRef mappingRef) {
        super(tmpId, name, null, previous, scope);
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
    public int execute(MetaFrame frame) {
        var sourceInst = (Reference) frame.pop();
        frame.push(mappingRef.resolve().map(sourceInst.resolve(), frame).getReference());
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("map " + mappingRef.resolve().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

}

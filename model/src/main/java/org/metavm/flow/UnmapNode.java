package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.UnmapNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.object.view.ObjectMappingRef;

@EntityType
public class UnmapNode extends NodeRT {

    public static UnmapNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (UnmapNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (UnmapNodeParam) nodeDTO.param();
            var mappingRef = ObjectMappingRef.create(param.mappingRef(), context);
            node = new UnmapNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, mappingRef);
        }
        return node;
    }

    private final ObjectMappingRef mappingRef;

    public UnmapNode(Long tmpId, @NotNull String name, @Nullable NodeRT previous, @NotNull ScopeRT scope,
                     ObjectMappingRef mappingRef) {
        super(tmpId, name, null, previous, scope);
        this.mappingRef = mappingRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnmapNode(this);
    }

    @Override
    protected UnmapNodeParam getParam(SerializeContext serializeContext) {
        return new UnmapNodeParam(mappingRef.toDTO(serializeContext));
    }

    @NotNull
    @Override
    public Type getType() {
        return mappingRef.resolve().getSourceType();
    }

    @Override
    public int execute(MetaFrame frame) {
        var viewInst = (Reference) frame.pop();
        frame.push(mappingRef.resolve().unmap(viewInst, frame));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("unmap " + mappingRef.resolve().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }
}

package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.GetUniqueNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Index;
import org.metavm.object.type.Types;
import org.metavm.object.type.UnionType;
import org.metavm.util.Instances;

@EntityType
public class GetUniqueNode extends NodeRT {

    public static GetUniqueNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        GetUniqueNode node = (GetUniqueNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            GetUniqueNodeParam param = nodeDTO.getParam();
            Index index = context.getEntity(Index.class, Id.parse(param.indexId()));
            var type = Types.getNullableType(index.getDeclaringType().getType());
            node = new GetUniqueNode(nodeDTO.tmpId(), nodeDTO.name(), (UnionType) type, index, prev, scope);
        }
        return node;
    }

    private Index index;

    public GetUniqueNode(Long tmpId, String name, UnionType type, Index index, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, type, previous, scope);
        this.index = index;
    }

    @Override
    protected GetUniqueNodeParam getParam(SerializeContext serializeContext) {
        return new GetUniqueNodeParam(index.getStringId());
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public int execute(MetaFrame frame) {
        Value result = frame.instanceRepository().selectFirstByKey(frame.loadIndexKey(index));
        if (result == null)
            result = Instances.nullInstance();
        frame.push(result);
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getUnique(" + index.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - index.getFields().size();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetUniqueNode(this);
    }
}

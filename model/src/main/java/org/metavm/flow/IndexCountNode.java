package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.IndexCountNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Index;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexCountNode extends NodeRT {

    public static IndexCountNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (IndexCountNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (IndexCountNodeParam) nodeDTO.param();
            var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
            node = new IndexCountNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, index);
        }
        return node;
    }

    private Index index;

    public IndexCountNode(Long tmpId, String name, NodeRT previous, ScopeRT scope, Index index) {
        super(tmpId, name, null, previous, scope);
        this.index = index;
    }

    @Override
    protected IndexCountNodeParam getParam(SerializeContext serializeContext) {
        return new IndexCountNodeParam(serializeContext.getStringId(index));
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public int execute(MetaFrame frame) {
        var to = frame.loadIndexKey(index);
        var from = frame.loadIndexKey(index);
        var count = frame.instanceRepository().indexCount(from, to);
        frame.push(Instances.longInstance(count));
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexCount(" + index.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (index.getFields().size() << 1);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexCountNode(this);
    }

    @NotNull
    @Override
    public Type getType() {
        return Types.getLongType();
    }
}

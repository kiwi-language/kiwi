package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.IndexCountNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Index;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexCountNode extends NodeRT {

    public static IndexCountNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (IndexCountNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (IndexCountNodeParam) nodeDTO.param();
            var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
            node = new IndexCountNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, index);
        }
        return node;
    }

    private Index index;

    public IndexCountNode(Long tmpId, String name, NodeRT previous, Code code, Index index) {
        super(tmpId, name, null, previous, code);
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
    public void writeContent(CodeWriter writer) {
        writer.write("indexCount(" + index.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (index.getFields().size() << 1);
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_COUNT);
        output.writeConstant(index.getRef());
    }

    @Override
    public int getLength() {
        return 3;
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

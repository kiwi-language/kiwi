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
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

@EntityType
public class IndexCountNode extends NodeRT {

    public static IndexCountNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (IndexCountNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (IndexCountNodeParam) nodeDTO.param();
            var indexRef = IndexRef.create(param.indexRef(), context);
            node = new IndexCountNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, indexRef);
        }
        return node;
    }

    private final IndexRef indexRef;

    public IndexCountNode(Long tmpId, String name, NodeRT previous, Code code, IndexRef indexRef) {
        super(tmpId, name, null, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    protected IndexCountNodeParam getParam(SerializeContext serializeContext) {
        return new IndexCountNodeParam(indexRef.toDTO(serializeContext));
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexCount(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (getIndex().getFields().size() << 1);
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_COUNT);
        output.writeConstant(indexRef);
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

package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.IndexScanNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Index;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexScanNode extends NodeRT {

    public static IndexScanNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (IndexScanNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (IndexScanNodeParam) nodeDTO.param();
            var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
            node = new IndexScanNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, index);
        }
        return node;
    }

    private Index index;

    public IndexScanNode(Long tmpId, String name, NodeRT previous, Code code, Index index) {
        super(tmpId, name, null, previous, code);
        this.index = index;
    }

    @Override
    protected IndexScanNodeParam getParam(SerializeContext serializeContext) {
        return new IndexScanNodeParam(serializeContext.getStringId(index));
    }

    @Override
    public ArrayType getType() {
        return new ArrayType(index.getDeclaringType().getType(), ArrayKind.READ_ONLY);
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexScan(" + index.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (index.getFields().size() << 1);
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SCAN);
        output.writeConstant(index.getRef());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexQueryNode(this);
    }
}

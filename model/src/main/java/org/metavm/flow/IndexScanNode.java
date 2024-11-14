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
import org.metavm.object.type.IndexRef;

@EntityType
public class IndexScanNode extends NodeRT {

    public static IndexScanNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (IndexScanNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (IndexScanNodeParam) nodeDTO.param();
            var indexRef = IndexRef.create(param.indexRef(), context);
            node = new IndexScanNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, indexRef);
        }
        return node;
    }

    private final IndexRef indexRef;

    public IndexScanNode(Long tmpId, String name, NodeRT previous, Code code, IndexRef indexRef) {
        super(tmpId, name, null, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    protected IndexScanNodeParam getParam(SerializeContext serializeContext) {
        return new IndexScanNodeParam(indexRef.toDTO(serializeContext));
    }

    @Override
    public ArrayType getType() {
        return new ArrayType(getIndex().getDeclaringType().getType(), ArrayKind.READ_ONLY);
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexScan(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - (getIndex().getFields().size() << 1);
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SCAN);
        output.writeConstant(indexRef);
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

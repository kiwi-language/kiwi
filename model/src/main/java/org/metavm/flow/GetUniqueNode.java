package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.GetUniqueNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Types;
import org.metavm.object.type.UnionType;

@EntityType
public class GetUniqueNode extends NodeRT {

    public static GetUniqueNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        GetUniqueNode node = (GetUniqueNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (GetUniqueNodeParam) nodeDTO.getParam();
            var indexRef = IndexRef.create(param.indexRef(), context);
            var type = Types.getNullableType(indexRef.resolve().getDeclaringType().getType());
            node = new GetUniqueNode(nodeDTO.tmpId(), nodeDTO.name(), (UnionType) type, indexRef, prev, code);
        }
        return node;
    }

    private final IndexRef indexRef;

    public GetUniqueNode(Long tmpId, String name, UnionType type, IndexRef indexRef, NodeRT previous, Code code) {
        super(tmpId, name, type, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    protected GetUniqueNodeParam getParam(SerializeContext serializeContext) {
        return new GetUniqueNodeParam(indexRef.toDTO(serializeContext));
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getUnique(" + getIndex().getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 1 - getIndex().getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_UNIQUE);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetUniqueNode(this);
    }
}

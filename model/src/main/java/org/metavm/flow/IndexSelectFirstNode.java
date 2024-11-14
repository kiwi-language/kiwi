
package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.IndexSelectFirstNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexSelectFirstNode extends NodeRT {

    public static IndexSelectFirstNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (IndexSelectFirstNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (IndexSelectFirstNodeParam) nodeDTO.param();
            var indexRef = IndexRef.create(param.indexRef(), context);
            node = new IndexSelectFirstNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, indexRef);
        }
        return node;
    }

    private final IndexRef indexRef;

    public IndexSelectFirstNode(Long tmpId, String name, NodeRT previous, Code code,
                                IndexRef indexRef) {
        super(tmpId, name, null, previous, code);
        this.indexRef = indexRef;
    }

    @Override
    protected IndexSelectFirstNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectFirstNodeParam(indexRef.toDTO(serializeContext));
    }

    public Index getIndex() {
        return indexRef.resolve();
    }

    @Override
    public Type getType() {
        return Types.getNullableType(indexRef.resolve().getDeclaringType().getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelectFirst(" + indexRef.resolve().getName() + ", " +  ")");
    }

    @Override
    public int getStackChange() {
        return 1 - indexRef.resolve().getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SELECT_FIRST);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexSelectFirstNode(this);
    }

}

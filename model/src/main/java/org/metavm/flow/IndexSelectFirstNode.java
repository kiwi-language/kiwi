
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
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexSelectFirstNode extends NodeRT {

    public static IndexSelectFirstNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var param = (IndexSelectFirstNodeParam) nodeDTO.param();
        var index = requireNonNull(context.getEntity(Index.class, Id.parse(param.indexId())));
        var node = (IndexSelectFirstNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new IndexSelectFirstNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, index);
        return node;
    }

    private Index index;

    public IndexSelectFirstNode(Long tmpId, String name, NodeRT previous, ScopeRT scope,
                                Index index) {
        super(tmpId, name, null, previous, scope);
        this.index = index;
    }

    @Override
    protected IndexSelectFirstNodeParam getParam(SerializeContext serializeContext) {
        return new IndexSelectFirstNodeParam(
                serializeContext.getStringId(index)
        );
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    @Override
    public Type getType() {
        return Types.getNullableType(index.getDeclaringType().getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelectFirst(" + index.getName() + ", " +  ")");
    }

    @Override
    public int getStackChange() {
        return 1 - index.getFields().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SELECT_FIRST);
        output.writeConstant(index.getRef());
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
